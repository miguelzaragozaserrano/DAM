package com.miguelzaragoza.upm.dam.modules.map

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import com.miguelzaragoza.upm.dam.model.Cameras

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
    lateinit var googleMap: GoogleMap

    init {
        try{
            MapsInitializer.initialize(context)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    /*************************** FUNCIONES PRIVADAS BÁSICAS ***************************/

    /**
     * Función que cambia el tipo de Mapa.
     * @param type: variable que contiene el tipo de mapa
     */
    fun changeTypeMap(type: Int){
        googleMap.mapType = type
    }

}