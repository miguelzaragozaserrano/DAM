package com.miguelzaragoza.upm.dam.modules.ui.map

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.miguelzaragoza.upm.dam.MapActivity
import com.miguelzaragoza.upm.dam.databinding.FragmentMapBinding
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.model.Cameras
import com.miguelzaragoza.upm.dam.modules.ui.map.MapViewModel.Companion.REQUEST_CODE
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

        /* Recogemos la lista de cámaras que se pasa entre activities y si queremos un
        *  activar el modo cluster o no */
        val mapActivity = activity as MapActivity
        val cluster = mapActivity.getCluster()
        val sharedList = mapActivity.getSharedList()
        val cameras = Cameras()
        cameras.addAll(sharedList)

        /* Guardamos la cámara seleccionada, para poder centrar el mapa */
        if(mapViewModel.camera == null){
            for(camera in cameras){
                if(camera.selected) mapViewModel.camera = camera
            }
        }

        /* Damos acceso binding a MapViewModel, al cluster y a las cámaras */
        binding.cameras = cameras
        binding.cluster = cluster
        binding.viewModel = mapViewModel

        /* Declaramos un iconLocation y googleMap en el ViewModel
        *  para poder actuar con ellos mediante la UI */
        mapViewModel.iconLocation = binding.iconLocation
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

        /* Observamos la variable askPermissions. Si toma un valor distinto de null,
        *  es debido a que se ha pulsado el icono de localización y queremos activar nuestra
        *  ubicación actual */
        mapViewModel.askPermissions.observe(viewLifecycleOwner, {
            if (it != null)
                requestPermissions(
                    arrayOf(
                        mapViewModel.fineLocationPermission
                    ), REQUEST_CODE
                )
        })

        return binding.root
    }

    /**
     * Función que se llama para solicitar los permisos.
     *
     * @param requestCode Código de petición.
     * @param permissions Lista de permisos.
     * @param grantResults Permisos concedidos.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE -> {
                /* En caso de que se haya concedido los permisos, calculamos la ruta hasta la
                *  cámara seleccionada desde nuestra ubicación */
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapViewModel.mLocationPermissionGranted = true
                    mapViewModel.getLocation()
                }
            }
        }
    }

    /**
     * Función que desactiva la ubicación del dispositivo cuando se destruye el Fragment.
     */
    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        if(mapViewModel.googleMap.isMyLocationEnabled){
            mapViewModel.googleMap.isMyLocationEnabled = false
            mapViewModel.googleMap.uiSettings.isMyLocationButtonEnabled = false
        }
    }

}