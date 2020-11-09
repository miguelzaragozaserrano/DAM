package com.miguelzaragoza.upm.dam.modules.cameras

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.miguelzaragoza.upm.dam.R
import com.miguelzaragoza.upm.dam.databinding.FragmentCamerasBinding
import com.miguelzaragoza.upm.dam.viewmodel.CamerasViewModelFactory

/******************************* VARIABLES CONSTANTES ******************************
 ***********************************************************************************/

const val NOT_ORDER = 0
const val ASCENDING_ORDER = 1
const val DESCENDING_ORDER = 2

/**
 * Fragment que muestra la primera pantalla
 * donde aparece la lista de cámaras y la imagen que capturan
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

    /* Variable privada para almacenar el SearchView */
    private lateinit var searchView: SearchView

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
       val binding = FragmentCamerasBinding.inflate(inflater)

        /* Permite a Data Binding observar LiveData con el lifecycle de su Fragment */
        binding.lifecycleOwner = this

        /* Damos acceso binding a CamerasViewModel */
        binding.viewModel = camerasViewModel
        binding.camerasList.adapter = camerasViewModel.adapter

        /* Indicamos que va a existir un menú de opciones */
        setHasOptionsMenu(true)

        /* Observamos la variable navigateToSelectedCamera. Si toma un valor distinto de null,
        *  es debido a que se ha pulsado una imagen y por tanto navegamos a un tercer fragment */
        camerasViewModel.navigateToSelectedCamera.observe(viewLifecycleOwner, {
            if (it != null) {
                saveQuery()
                camerasViewModel.setSharedList()
                findNavController()
                        .navigate(CamerasFragmentDirections
                                .actionCamerasFragmentToMapsFragment(camerasViewModel.sharedList)
                        )
            }
        })

        /* Recogemos la lista de cámaras que se pasa entre fragmentos solamente si la lista
        *  del ViewModel está vacía (primera vez que se accede al Fragment) */
        if(camerasViewModel.list.isEmpty()){
            camerasViewModel.list.addAll(CamerasFragmentArgs.fromBundle(requireArguments()).cameras)
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
        menu.findItem(R.id.order_icon).icon = camerasViewModel.iconOrder
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
                object: MenuItem.OnActionExpandListener{
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
                        if(camerasViewModel.querySearched == "")
                            camerasViewModel.adapter.filterByName(query)
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
                                .getDrawable(requireContext(), R.drawable.ic_descending_order)!!
                        /* Guardamos los estados */
                        camerasViewModel.order = DESCENDING_ORDER
                    }
                    DESCENDING_ORDER -> {
                        /* Mostramos la lista */
                        camerasViewModel.adapter.filterDescending()
                        /* Cambiamos el icono */
                        item.icon = ContextCompat
                                .getDrawable(requireContext(), R.drawable.ic_ascending_order)!!
                        /* Guardamos los estados */
                        camerasViewModel.order = ASCENDING_ORDER
                    }
                }
                /* Guardamos el nuevo valor del iconOrder */
                camerasViewModel.iconOrder = item.icon
                return true
            }
            R.id.action_reset -> {
                /* Si pulsamos la opción de resetear la lista, volvemos al LoadingFragment */
                findNavController()
                        .navigate(CamerasFragmentDirections
                                .actionCamerasFragmentToSplashFragment()
                        )
                return true
            }
            R.id.action_all -> {
                /* Si pulsamos la opción de mostrar todas las cámaras,
                *  actualizamos el valor */
                camerasViewModel.showAllCameras = !camerasViewModel.showAllCameras
                item.isChecked = !item.isChecked
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

}