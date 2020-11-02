package com.miguelzaragoza.upm.dam.modules.cameras

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.miguelzaragoza.upm.dam.R
import com.miguelzaragoza.upm.dam.databinding.FragmentCamerasBinding
import com.miguelzaragoza.upm.dam.viewmodel.CamerasViewModelFactory

/**
 * Fragment que muestra la primera pantalla
 * donde aparece la lista de cámaras y la imagen que capturan
 */
class CamerasFragment : Fragment() {

    /******************************** VARIABLES BÁSICAS ********************************/
    /* Variable que declara el ViewModel para poder interactuar con él */
    private lateinit var camerasViewModel: CamerasViewModel

    /* Variable que guarda el id del modo seleccionado */
    private var modeSelected: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /* Comprobamos si tenemos algún id guardado con anterioridad */
        if(savedInstanceState != null){
            modeSelected = savedInstanceState.getInt("modeSelected")
        }

        /* Indicamos que vamos a añadir un menú de opciones */
        setHasOptionsMenu(true)

        /* Usamos la librería DataBinding para inflar el Fragmment con el layout correspondiente */
        val binding = FragmentCamerasBinding.inflate(inflater)

        /* Obtenemos un objeto Application para pasarselo como parámetro al ViewModel y
        *  de esa manera poder obtener el contexto correspondiente allí */
        val application = requireNotNull(this.activity).application

        /* Como necesitamos pasarle un parámetro al ViewModel por el constructor
        *  tenemos que crear un ViewModelFactory */
        val viewModelFactory = CamerasViewModelFactory(application)

        /* Declaramos nuestra variable del ViewModel para poder interactuar con ella */
        camerasViewModel = ViewModelProvider(this, viewModelFactory).get(CamerasViewModel::class.java)

        /* Asignamos al lifecycleOwner el fragment actual para detectar
        *  los cambios de los objetos LiveData */
        binding.lifecycleOwner = this

        /* Asignamos un valor a la variable viewModel del XML unido a este Fragment
        *  con el valor que nos interesa */
        binding.viewModel = camerasViewModel

        /* Le asignamos al adaptador del RecyclerView del fichero XML,
        *  el objeto CamerasAdapter creado en el ViewModel */
        binding.camerasList.adapter = camerasViewModel.adapter

        /* Observamos la variable navigateToSelectedCamera. Si toma un valor distinto de null,
        *  es debido a que se ha pulsado una imagen y por tanto navegamos a un segundo fragment */
        camerasViewModel.navigateToSelectedCamera.observe(viewLifecycleOwner, {
            if(it != null && camerasViewModel.camera.value != null){
                camerasViewModel.setSharedList()
                findNavController().navigate(CamerasFragmentDirections.actionCamerasFragmentToMapsFragment(camerasViewModel.sharedList))
            }
        })

        /* Devolvemos la vista más externa del layout asociado con el binding */
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        /* Guardamos el id del modo que está seleccionado */
        outState.putInt("modeSelected", modeSelected)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        /* Inflamos el menú */
        inflater.inflate(R.menu.menu, menu)
        if(modeSelected != 0) menu.findItem(modeSelected).isChecked = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /* Analizamos la opción seleccionada y realizamos la tarea necesaria */
        when(item.itemId){
            R.id.action_asc -> camerasViewModel.getAscendingList()
            R.id.action_desc -> camerasViewModel.getDescendingList()
            R.id.single_camera -> {
                camerasViewModel.setMode(true)
                item.isChecked = true
                modeSelected = item.itemId
            }
            R.id.multiple_cameras -> {
                camerasViewModel.setMode(false)
                item.isChecked = true
                modeSelected = item.itemId
            }
            else -> return false
        }
        return super.onOptionsItemSelected(item)
    }

}