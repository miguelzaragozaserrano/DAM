package com.miguelzaragoza.upm.dam.modules.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.miguelzaragoza.upm.dam.databinding.FragmentMapBinding
import com.miguelzaragoza.upm.dam.viewmodel.MapViewModelFactory

/**
 * Fragment que muestra la segunda pantalla
 * donde aparece el mapa de Google
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

        /* Declaramos nuestra variable del ViewModel para poder interactuar con él */
        val mapViewModel = ViewModelProvider(this, viewModelFactory).get(MapViewModel::class.java)

        /* Recogemos la lista de cámaras que se pasa entre fragmentos */
        val cameras = MapFragmentArgs.fromBundle(arguments!!).cameras

        /* Asignamos al lifecycleOwner el fragment actual para detectar
        *  los cambios de los objetos LiveData */
        binding.lifecycleOwner = this

        /* Creamos e inicializamos el MapView */
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.onResume()
        binding.mapView.getMapAsync {
            mapViewModel.initialSetup(it, cameras)
        }

        /* Dependiendo del chip seleccionado, se mostrará un modo u otro */
        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                binding.hybridChip.id -> mapViewModel.changeTypeMap(GoogleMap.MAP_TYPE_HYBRID)
                binding.satelliteChip.id -> mapViewModel.changeTypeMap(GoogleMap.MAP_TYPE_SATELLITE)
                binding.normalChip.id -> mapViewModel.changeTypeMap(GoogleMap.MAP_TYPE_NORMAL)
            }
        }

        /* Devolvemos la vista más externa del layout asociado con el binding */
        return binding.root
    }

}