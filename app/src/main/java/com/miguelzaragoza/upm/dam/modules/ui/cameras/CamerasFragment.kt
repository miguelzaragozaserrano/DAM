package com.miguelzaragoza.upm.dam.modules.ui.cameras

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.miguelzaragoza.upm.dam.MapActivity
import com.miguelzaragoza.upm.dam.R
import com.miguelzaragoza.upm.dam.databinding.FragmentCamerasBinding
import com.miguelzaragoza.upm.dam.viewmodel.CamerasViewModelFactory

/******************************* VARIABLES CONSTANTES ******************************
 ***********************************************************************************/

const val NOT_ORDER = 0
const val ASCENDING_ORDER = 1
const val DESCENDING_ORDER = 2

const val NORMAL_MODE = 0
const val FAV_MODE = 1

/**
 * Fragment que muestra la primera pantalla
 * donde aparece la lista de cámaras y la imagen que capturan.
 */
class CamerasFragment : Fragment() {

    /******************************** VARIABLES BÁSICAS ********************************
     ***********************************************************************************/

    /**
     * Inicializamos con lazy nuestro [CamerasViewModel] para crearlo con un método
     * lifecycle apropiado.
     */
    private val camerasViewModel: CamerasViewModel by lazy{
        val application = requireNotNull(this.activity).application
        ViewModelProvider(this, CamerasViewModelFactory(application))
                .get(CamerasViewModel::class.java)
    }

    /* Variables privadas */
    private var enabled = true
    private lateinit var searchView: SearchView
    private lateinit var ivCamera: ImageView
    private lateinit var iconOrder: MenuItem

    /******************************* FUNCIONES OVERRIDE *******************************
     **********************************************************************************/

    /**
     * Función que se llama para instanciar la vista de interfaz de usuario (UI).
     *
     * @param inflater El objeto LayoutInflater se usa para inflar cualquier vista
     * en el Fragment.
     * @param container Si no es nulo, esta es la vista principal a la que se debe adjuntar
     * la UI del Fragment. El Fragment no debe agregar la vista en sí, pero esto puede
     * usarse para generar los LayoutParams de la vista.
     * @param savedInstanceState Si no es nulo, este Fragment se está reconstruyendo a partir
     * de un estado guardado anterior como se indica aquí.
     *
     * @return Devuelve la vista UI del Fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* Indicamos que va a existir un menú de opciones */
        setHasOptionsMenu(true)

       val binding = FragmentCamerasBinding.inflate(inflater)

        /* Permite a Data Binding observar LiveData con el lifecycle de su Fragment */
        binding.lifecycleOwner = this

        /* Damos acceso binding a CamerasViewModel y al adapter */
        binding.viewModel = camerasViewModel
        binding.camerasList.adapter = camerasViewModel.adapter

        /* Guardamos el ImageView como variable */
        ivCamera = binding.cameraImage

        /* Observamos la variable navigateToSelectedCamera. Si toma un valor distinto de null,
        *  es debido a que se ha pulsado una imagen y por tanto navegamos al segundo activity */
        camerasViewModel.navigateToSelectedCamera.observe(viewLifecycleOwner, {
            if (it != null) {
                saveQuery()
                camerasViewModel.setSharedList()
                val intent = Intent(context, MapActivity::class.java)
                intent.putParcelableArrayListExtra(
                    "sharedList", camerasViewModel.sharedList as ArrayList<out Parcelable?>?)
                intent.putExtra("cluster", camerasViewModel.cluster)
                startActivity(intent)
            }
        })

        /* Observamos el tamaño de la base de datos para permitir
        *  resetear la lista de favoritos o no */
        camerasViewModel.database.getSize().observe(viewLifecycleOwner, { size ->
            enabled = size > 0
            camerasViewModel.optionReset.isEnabled = enabled
        })

        /* Recogemos la lista de cámaras que se pasa entre fragmentos solamente si la lista
        *  del ViewModel está vacía (primera vez que se accede al Fragment) */
        if(camerasViewModel.list.isEmpty()){
            camerasViewModel.list.addAll(
                CamerasFragmentArgs
                    .fromBundle(requireArguments()).cameras
            )
        }

        return binding.root
    }

    /**
     * Función que se llama para inflar el menú de opciones.
     *
     * @param menu Menú que queremos inflar.
     * @param inflater El objeto MenuInflater lo utilizamos para inflar el menú.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)

        /* Asignamos los valores correspondientes */
        iconOrder = menu.findItem(R.id.order_icon)
        iconOrder.icon = camerasViewModel.iconOrder!!
        camerasViewModel.optionReset = menu.findItem(R.id.action_reset)
        menu.findItem(R.id.fav_icon).icon = camerasViewModel.iconFav
        menu.findItem(R.id.action_all).isChecked = camerasViewModel.showAllCameras

        /* Inicialiazamos nuestro SearchView */
        val iconSearch = menu.findItem(R.id.search_icon)
        searchView = iconSearch.actionView as SearchView

        /* Le asignamos un hint para ayudar al usuario y el máximo ancho posible */
        searchView.queryHint = getString(R.string.search_query_hint)
        searchView.maxWidth = Integer.MAX_VALUE

        /* Declaramos un setOnActionExpandListener para ocultar los iconos y así poder
        *  expandir el SearchView cuando escribimos. Además, guardamos el valor focus
        *  para saber si cuando cambiamos de Fragment, teníamos el SearchView activo o no */
        iconSearch.setOnActionExpandListener(
            object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                    setItemsVisibility(menu, iconSearch, false)
                    camerasViewModel.focus = true
                    return true
                }

                override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                    setItemsVisibility(menu, iconSearch, true)
                    camerasViewModel.focus = false
                    return true
                }
            })

        /* Declaramos un setOnQueryListener para que cada vez que cambie el texto del SearchView
        *  nos filtre la lista que nos interesa. En caso de que pulsemos la lupa, cierra el
        *  teclado */
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(query: String?): Boolean {
                    if (camerasViewModel.querySearched == "")
                        camerasViewModel.camera.value?.let {
                            camerasViewModel.adapter.filterByName(query, ivCamera,
                                it
                            )
                        }
                    return true
                }
            })

        /* En caso de que el SearchView estuviera activo, lo volvemos a abrir y le asignamos
        *  el valor de la query que tenía escrita */
        if(camerasViewModel.focus){
            iconSearch.expandActionView()
            searchView.isIconified = false
            searchView.clearFocus()
            searchView.setQuery(camerasViewModel.querySearched, false)
            camerasViewModel.querySearched = ""
        }

    }

    /**
     * Función que gestiona el item del menú seleccionado.
     *
     * @param item Elemento seleccionado del menú.
     *
     * @return Nos devuelve si se ha completado la acción o no.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /* Analizamos la opción seleccionada y realizamos la tarea necesaria */
        when(item.itemId){
            R.id.order_icon -> {
                /* Si pulsamos el icono de ordenar, analizamos el estado en el que se encuentra */
                when (camerasViewModel.order) {
                    ASCENDING_ORDER, NOT_ORDER -> {
                        /* Mostramos la lista */
                        camerasViewModel.adapter.filterAscending()
                        /* Cambiamos el icono */
                        item.icon = ContextCompat
                            .getDrawable(requireContext(), R.drawable.ic_descending_order)
                        /* Guardamos el estado */
                        camerasViewModel.order = DESCENDING_ORDER
                    }
                    DESCENDING_ORDER -> {
                        /* Mostramos la lista */
                        camerasViewModel.adapter.filterDescending()
                        /* Cambiamos el icono */
                        item.icon = ContextCompat
                            .getDrawable(requireContext(), R.drawable.ic_ascending_order)
                        /* Guardamos el estado */
                        camerasViewModel.order = ASCENDING_ORDER
                    }
                }
                /* Guardamos el valor del icono orden */
                camerasViewModel.iconOrder = item.icon
                return true
            }
            R.id.fav_icon -> {
                /* Si pulsamos el icono de favorito, analizamos el estado en el que se encuentra */
                when (camerasViewModel.mode) {
                    NORMAL_MODE -> {
                        /* Cambiamos el icono */
                        item.icon = ContextCompat
                            .getDrawable(requireContext(), R.drawable.ic_favorite_on)
                        /* Desactivamos el botón de ordenar */
                        iconOrder.icon = ContextCompat
                            .getDrawable(requireContext(), R.drawable.ic_order_disabled)
                        iconOrder.isEnabled = false
                        /* Cambiamos el modo del adaptador */
                        camerasViewModel.adapter.setMode(FAV_MODE)
                        /* Mostramos la lista de favoritos */
                        camerasViewModel.adapter.showFavoriteList()
                        /* Analizamos si tenemos una cámara seleccionada */
                        if (camerasViewModel.camera.value != null)
                        /* Y en caso de que al cambiar no sea favorita */
                            if (!camerasViewModel.camera.value!!.fav) {
                                /* Quitamos la imagen y la cámara */
                                ivCamera.setImageDrawable(null)
                                camerasViewModel.resetSelectedCamera()
                            }
                        /* Guardamos el estado */
                        camerasViewModel.mode = FAV_MODE
                        /* Cambiamos el estado de la opción de resetear */
                        camerasViewModel.optionReset.isEnabled = enabled
                    }
                    FAV_MODE -> {
                        /* Cambiamos el icono */
                        item.icon = ContextCompat
                            .getDrawable(requireContext(), R.drawable.ic_favorite_off)
                        /* Actiamos y recuperamos el botón de ordenar */
                        iconOrder.icon = camerasViewModel.iconOrder
                        iconOrder.isEnabled = true
                        /* Cambiamos el modo del adaptador */
                        camerasViewModel.adapter.setMode(NORMAL_MODE)
                        /* Mostramos la lista normal */
                        camerasViewModel.adapter.showNormalList()
                        /* Guardamos el estado */
                        camerasViewModel.mode = NORMAL_MODE
                        /* Cambiamos el estado de la opción de resetear */
                        camerasViewModel.optionReset.isEnabled = true
                    }
                }
                /* Guardamos el valor del icono favoritos */
                camerasViewModel.iconFav = item.icon
                return true
            }
            R.id.action_reset -> {
                /* Si pulsamos la opción de resetear la lista,
                *  analizamos el estado en el que se encuentra */
                when (camerasViewModel.mode) {
                    NORMAL_MODE -> {
                        /* Volvemos al LoadingFragment */
                        findNavController()
                            .navigate(
                                CamerasFragmentDirections
                                    .actionCamerasFragmentToSplashFragment()
                            )
                        return true
                    }
                    FAV_MODE -> {
                        /* Mostramos el diálogo para advertir del reseteo */
                        showDialogReset()
                        return true
                    }
                }
                return true
            }
            R.id.action_all -> {
                /* Si pulsamos la opción de mostrar todas las cámaras,
                *  actualizamos el valor y, en caso de marcarlo, preguntamos si queremos Cluster */
                camerasViewModel.showAllCameras = !camerasViewModel.showAllCameras
                item.isChecked = !item.isChecked
                if (item.isChecked) showDialogCluster()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /*************************** FUNCIONES PRIVADAS BÁSICAS ***************************
     **********************************************************************************/

    /**
     * Función que nos permite guardar la query del SearchView para volverla a mostrar
     * cuando volvamos a este Fragment.
     */
    private fun saveQuery(){
        camerasViewModel.querySearched = searchView.query.toString()
    }

    /**
     * Función que nos permite mostrar u ocultar los items del menú.
     *
     * @param menu Menú al que queremos ocultar o mostrar los items.
     * @param searchItem Item que queremos que siempre esté visible.
     * @param visible Variable que determina si mostramos u ocultamos el item.
     */
    private fun setItemsVisibility(menu: Menu, searchItem: MenuItem, visible: Boolean){
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item !== searchItem) item.isVisible = visible
        }
    }

    /**
     * Función que nos muestra un AlertDialog preguntando si queremos un Cluster en el MapView.
     */
    private fun showDialogCluster(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.alert_cluster_title))
            .setMessage(getString(R.string.alert_cluster_message))
            .setPositiveButton(
                getString(android.R.string.ok)
            ) { _, _ ->
                camerasViewModel.cluster = true
            }
            .setNegativeButton(
                getString(R.string.cancel_button)
            ) { _, _ ->
                camerasViewModel.cluster = false
            }
        builder.create()
        builder.show()
    }

    /**
     * Función que nos muestra un AlertDialog preguntando si estamos seguros de querer resetear
     * la lista de favoritos.
     */
    private fun showDialogReset(){
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.alert_reset_title))
            .setMessage(getString(R.string.alert_reset_message))
            .setPositiveButton(
                getString((android.R.string.ok))
            ) { _, _ ->
                camerasViewModel.reset()
                ivCamera.setImageDrawable(null)
            }
            .setNegativeButton(
                getString(R.string.cancel_button)
            ) { _, _ ->
            }
        builder.create()
        builder.show()
    }

}