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
 * Fragment que muestra la tercera pantalla
 * donde aparece el mapa con las cámaras.
 */
class MapFragment : Fragment() {

    /******************************** VARIABLES BÁSICAS ********************************
     ***********************************************************************************/

    /**
     * Inicializamos con lazy nuestro [MapViewModel] para crearlo con un método
     * lifecycle apropiado.
     */
    private val mapViewModel: MapViewModel by lazy{
        val application = requireNotNull(this.activity).application
        ViewModelProvider(this, MapViewModelFactory(application))
                .get(MapViewModel::class.java)
    }

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
        val binding = FragmentMapBinding.inflate(inflater)

        /* Permite a Data Binding observar LiveData con el lifecycle de su Fragment */
        binding.lifecycleOwner = this

        /* Recogemos la lista de cámaras que se pasa entre fragmentos */
        val cameras = MapFragmentArgs.fromBundle(requireArguments()).cameras

        /* Damos acceso binding a MapViewModel */
        binding.cameras = cameras
        binding.mapView.getMapAsync {
            mapViewModel.googleMap = it
        }

        /* Dependiendo del chip seleccionado, se mostrará un tipo de mapa u otro */
        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                binding.hybridChip.id -> mapViewModel.changeTypeMap(GoogleMap.MAP_TYPE_HYBRID)
                binding.satelliteChip.id -> mapViewModel.changeTypeMap(GoogleMap.MAP_TYPE_SATELLITE)
                binding.normalChip.id -> mapViewModel.changeTypeMap(GoogleMap.MAP_TYPE_NORMAL)
            }
        }

        return binding.root
    }

}