package com.miguelzaragoza.upm.dam.ui.map

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.miguelzaragoza.upm.dam.model.Cameras
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel que realizará las funciones lógicas y almacenará los datos del
 * MapFragment.
 * @param application: objeto Application que nos permitirá obtener el contexto de la aplicación
 */
class MapViewModel (application: Application): AndroidViewModel(application) {

    /******************************** VARIABLES BÁSICAS ********************************/
    /* Variables privadas para definir el contexto cuando sea necesario,
    *  y para asignarle los atributos y valores necesarios al MapView */
    private val context = application.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var googleMap: GoogleMap

    /**
     * Función que inicializa el MapView con los valores deseados.
     * @param map: mapa al que le asignamos los valores iniciales
     */
    fun initialSetup(map: GoogleMap, cameras: Cameras){
        coroutineScope.launch {
            try{
                MapsInitializer.initialize(context)
            }catch (e: Exception){
                e.printStackTrace()
            }
            googleMap = map
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            addCameras(cameras)
            googleMap.addMarker(MarkerOptions().position(LatLng(-34.0, 151.0)).title("Marker Title").snippet("Marker Description"))
            val cameraPosition = CameraPosition.Builder().target(LatLng(-34.0, 151.0)).zoom(12F).build()
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    /**
     * Función que cambia el tipo de Mapa.
     * @param type: Int que contiene el tipo de mapa.
     */
    fun changeTypeMap(type: Int){
        googleMap.mapType = type
    }

    private fun addCameras(cameras: Cameras){
        Log.d("HOLI", cameras[1].coordinates)
    }

}