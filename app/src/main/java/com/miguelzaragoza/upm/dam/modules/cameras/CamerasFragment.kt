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
        ViewModelProvider(this)
                .get(CamerasViewModel::class.java)
    }

    private lateinit var searchView: SearchView

    /******************************* FUNCIONES OVERRIDE *******************************
     **********************************************************************************/

    /**
     * Función que se llama inmediatamente después del return de onCreateView() y el
     * se haya creado la jerarquía de vistas del Fragment. Se puede utilizar para recuperar vistas
     * o restaurar estados.
     *
     * @param view Vista creada recientemente.
     * @param savedInstanceState Si no es nulo, este Fragment se está reconstruyendo a partir
     * de un estado guardado anterior como se indica aquí.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* Observamos la variable navigateToSelectedCamera. Si toma un valor distinto de null,
        *  es debido a que se ha pulsado una imagen y por tanto navegamos a un segundo fragment */
        camerasViewModel.navigateToSelectedCamera.observe(viewLifecycleOwner, {
            if (it != null && camerasViewModel.camera.value != null) {
                saveQuery()
                camerasViewModel.setSharedList()
                findNavController()
                        .navigate(CamerasFragmentDirections
                                .actionCamerasFragmentToMapsFragment(camerasViewModel.sharedList)
                        )
            }
        })
    }

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

        /* Recogemos la lista de cámaras que se pasa entre fragmentos solamente si la lista
        *  de CamerasFragment está vacía */
        if(camerasViewModel.list.isEmpty()){
            val cameras = CamerasFragmentArgs.fromBundle(requireArguments()).cameras
            camerasViewModel.list.addAll(cameras)
            camerasViewModel.sharedList.addAll(cameras)
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

        /* Analizamos el estado en el que estábamos cuando regresamos al Fragment */
        when(camerasViewModel.order){
            ASCENDING_ORDER -> {
                /* Mostramos la lista */
                camerasViewModel.adapter.filterDescending()
                /* Cambiamos el icono */
                menu.findItem(R.id.order_icon).icon = ContextCompat
                    .getDrawable(requireContext(), R.drawable.ic_ascending_order)
            }
            DESCENDING_ORDER -> {
                /* Mostramos la lista */
                camerasViewModel.adapter.filterAscending()
                /* Cambiamos el icono */
                menu.findItem(R.id.order_icon).icon = ContextCompat
                    .getDrawable(requireContext(), R.drawable.ic_descending_order)
            }
        }

        /* Inicialiazamos nuestro SearchView */
        val iconSearch = menu.findItem(R.id.search_icon)
        searchView = iconSearch.actionView as SearchView

        /* Le asignamos un hint para ayudar al usuario y el máximo ancho posible */
        searchView.queryHint = getString(R.string.search_query_hint)
        searchView.maxWidth = Integer.MAX_VALUE

        /* Declaramos un setOnActionExpandListener para ocultar los iconos y así poder
        *  expandir el SearchView. Además guardamos el valor focus para saber si cuando
        *  cambiamos de Fragment, teníamos el SearchView activo o no */
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
        }

        /* Comprobamos si la opción de mostrar todas las cámaras estaba seleccionada o no */
        if(camerasViewModel.showAllCameras) menu.findItem(R.id.action_all).isChecked = true
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
                        /* Cambiamos de estado */
                        camerasViewModel.order = DESCENDING_ORDER
                    }
                    DESCENDING_ORDER -> {
                        /* Mostramos la lista */
                        camerasViewModel.adapter.filterDescending()
                        /* Cambiamos el icono */
                        item.icon = ContextCompat
                                .getDrawable(requireContext(), R.drawable.ic_ascending_order)
                        /* Cambiamos de estado */
                        camerasViewModel.order = ASCENDING_ORDER
                    }
                }
                return true
            }
            R.id.action_reset -> {
                /* Si pulsamos la opción de resetear la lista, volvemos al Fragment anterior */
                findNavController()
                        .navigate(CamerasFragmentDirections
                                .actionCamerasFragmentToSplashFragment()
                        )
                return true
            }
            R.id.action_all -> {
                /* Si pulsamos la opción de mostrar todas las cámaras,
                *  analizamos si está clickeado o no, y actuamos en función a eso */
                if(camerasViewModel.showAllCameras) camerasViewModel.setMode(false)
                else camerasViewModel.setMode(true)
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