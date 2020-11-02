package com.miguelzaragoza.upm.dam.modules.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import com.miguelzaragoza.upm.dam.model.Cameras
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel que realizará las funciones lógicas y almacenará los datos del
 * MapFragment.
 * @param application: objeto Application que nos permitirá obtener el contexto de la aplicación
 */
class MapViewModel (application: Application): AndroidViewModel(application) {

    /******************************* VARIABLES BÁSICAS ********************************/
    /* Variables privadas para definir el contexto cuando sea necesario,
    *  y para asignarle los atributos y valores necesarios al MapView */
    private val context = application.applicationContext
    private lateinit var googleMap: GoogleMap

    /*************************** FUNCIONES PRIVADAS BÁSICAS ***************************/
    /**
     * Función que inicializa el MapView con los valores deseados.
     * @param map: mapa al que le asignamos los valores iniciales
     */
    fun initialSetup(map: GoogleMap, cameras: Cameras){
        try{
            MapsInitializer.initialize(context)
        }catch (e: Exception){
            e.printStackTrace()
        }
        googleMap = map
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE

        val cameraPosition = CameraPosition.Builder().target(cameras[0].coordinates).zoom(12F).build()
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        addCameras(cameras)
    }

    /**
     * Función que cambia el tipo de Mapa.
     * @param type: variable que contiene el tipo de mapa
     */
    fun changeTypeMap(type: Int){
        googleMap.mapType = type
    }

    /**
     * Función que permite añadir los marcadores al mapa
     * @param cameras: lista de cámaras que queremos añadir al mapa
     */
    private fun addCameras(cameras: Cameras){
        cameras.map {camera ->
            googleMap.addMarker(
                    MarkerOptions()
                            .position(camera.coordinates)
                            .title(camera.name)
            )
        }
    }

}