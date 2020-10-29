package com.miguelzaragoza.upm.dam.ui.cameras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.miguelzaragoza.upm.dam.databinding.FragmentCamerasBinding
import com.miguelzaragoza.upm.dam.viewmodel.CamerasViewModelFactory

/**
 * Fragment que muestra la imagen de la cámara que seleccionamos.
 */
class CamerasFragment : Fragment() {
    /* Función que se invoca cada vez que se crea el Fragment */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        /* Usamos la librería DataBinding para inflar el Fragmment con el layout correspondiente */
        val binding = FragmentCamerasBinding.inflate(inflater)

        /* Obtenemos un objeto Application para pasarselo como parámetro al ViewModel y
        *  de esa manera poder obtener el contexto correspondiente allí */
        val application = requireNotNull(this.activity).application

        /* Como necesitamos pasarle un parámetro al ViewModel por el constructor
        *  tenemos que crear un ViewModelFactory */
        val viewModelFactory = CamerasViewModelFactory(application)

        /* Declaramos nuestra variable del ViewModel para poder interactuar con ella */
        val camerasViewModel = ViewModelProvider(this, viewModelFactory).get(CamerasViewModel::class.java)

        /* Asignamos al lifecycleOwner el Activity actual para detectar
        *  los cambios de los objetos LiveData */
        binding.lifecycleOwner = this

        /* Asignamos un valor a la variable viewModel del XML unido a este Fragment
        *  con el valor que nos interesa */
        binding.viewModel = camerasViewModel

        /* Le asignamos al adaptador del RecyclerView del fichero XML,
        *  el objeto CamerasAdapter creado en el ViewModel */
        binding.camerasList.adapter = camerasViewModel.adapter

        camerasViewModel.navigateToSelectedCamera.observe(viewLifecycleOwner, Observer {
            if(it != null) findNavController().navigate(CamerasFragmentDirections.actionCamerasFragmentToMapFragment())
        })

        /* Devolvemos la vista más externa del layout asociado con el binding */
        return binding.root
    }

}