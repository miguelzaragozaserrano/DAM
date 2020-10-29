package com.miguelzaragoza.upm.dam.ui.map

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.miguelzaragoza.upm.dam.databinding.FragmentMapBinding
import com.miguelzaragoza.upm.dam.viewmodel.MapViewModelFactory

/**
 * Fragment que muestra el mapa de la cámara seleccionada
 */
class MapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* Usamos la librería DataBinding para inflar el Fragmment con el layout correspondiente */
        val binding = FragmentMapBinding.inflate(inflater)

        /* Obtenemos un objeto Application para pasarselo como parámetro al ViewModel y
        *  de esa manera poder obtener el contexto correspondiente allí */
        val application = requireNotNull(this.activity).application

        /* Como necesitamos pasarle un parámetro al ViewModel por el constructor
        *  tenemos que crear un ViewModelFactory */
        val viewModelFactory = MapViewModelFactory(application)

        /* Declaramos nuestra variable del ViewModel para poder interactuar con ella */
        val mapViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(MapViewModel::class.java)

        /* Asignamos al lifecycleOwner el Activity actual para detectar
        *  los cambios de los objetos LiveData */
        binding.lifecycleOwner = requireActivity()

        /* Devolvemos la vista más externa del layout asociado con el binding */
        return binding.root
    }

}