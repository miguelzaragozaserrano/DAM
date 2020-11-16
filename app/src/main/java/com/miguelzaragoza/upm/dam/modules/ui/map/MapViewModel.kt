package com.miguelzaragoza.upm.dam.modules.ui.map

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.miguelzaragoza.upm.dam.R
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.model.Response
import com.miguelzaragoza.upm.dam.modules.utils.decodePoly
import com.miguelzaragoza.upm.dam.modules.utils.hasConnection

/**
 * ViewModel que realizará las funciones lógicas y almacenará los datos del
 * MapFragment.
 *
 * @param application: objeto Application que nos permitirá obtener el contexto de la aplicación
 */
class MapViewModel(application: Application): AndroidViewModel(application) {

    /******************************** VARIABLES BÁSICAS ********************************
     ***********************************************************************************/

    /* Variables privadas para definir el contexto cuando sea necesario */
    private val context = application.applicationContext

    /* Variable que almacena el objeto Polyline con la ruta */
    private lateinit var polyline: Polyline
    /* Variable que almacena la ubicación del dispositivo */
    private lateinit var myLocation: LatLng
    /* Variable que nos permite obtener la última ubicación del dispositivo */
    @SuppressLint("VisibleForTests")
    private var fusedLocationClient: FusedLocationProviderClient = FusedLocationProviderClient(context)

    /* Variable que almacena la cámara seleccionada */
    var camera: Camera? = null
    /* Variable que nos permite configurar el mapa */
    lateinit var googleMap: GoogleMap
    /* Variable que almacena el icono de la ubicación */
    lateinit var iconLocation: ImageView
    /* Variable que determina si tenemos permisos o no */
    var mLocationPermissionGranted = false
    /* Variable que almacena el permiso de la ubicación precisa */
    val fineLocationPermission = android.Manifest.permission.ACCESS_FINE_LOCATION

    /****************************** VARIABLE ENCAPSULADA ******************************
     **********************************************************************************/

    /* Variable para controlar la solicitud de permisos */
    private val _askPermissions = MutableLiveData<Boolean>()
    val askPermissions: LiveData<Boolean>
        get() = _askPermissions

    /********************************* COMPANION OBJECT ********************************
     **********************************************************************************/

    companion object{
        const val REQUEST_CODE = 100
    }

    /********************************* BLOQUE INICIAL *********************************
     **********************************************************************************/

    init {
        try{
            MapsInitializer.initialize(context)
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    /******************************** FUNCIONES BÁSICAS ********************************
     ***********************************************************************************/

    /**
     * Función que cambia el tipo de Mapa.
     *
     * @param type Variable que contiene el tipo de mapa.
     */
    fun changeTypeMap(type: Int){
        googleMap.mapType = type
    }

    /***************************** FUNCIONES LOCALIZACIÓN ******************************
     ***********************************************************************************/

    /**
     * Función que se activa cuando se pulsa el botón de la ubicación. Actua dependiendo de si
     * estaba activo o no.
     */
    @SuppressLint("MissingPermission")
    fun setLocation(){
        /* En caso de no estar activo, comprobamos si hay conexión */
        if(!googleMap.isMyLocationEnabled){
            if(hasConnection(getApplication<Application>())){
                /* Si hay conexión, obtenemos los permisos y cambiamos el icono */
                getLocationPermission()
                iconLocation.setImageResource(R.drawable.ic_marker_on)
            }else
                /* Si no hay conexión, mostramos un mensaje */
                Toast
                    .makeText(context, context.getString(R.string.check_connection), Toast.LENGTH_LONG)
                    .show()

        }else{
            /* Si estaba activo, lo desactivamos, eliminamos la ruta y cambiamos el icono */
            googleMap.isMyLocationEnabled = false
            googleMap.uiSettings.isMyLocationButtonEnabled = false
            polyline.remove()
            iconLocation.setImageResource(R.drawable.ic_marker_off)
        }
    }

    /**
     * Función que nos devuelve la localización en caso de que ya tengamos los permisos aceptados,
     * o que pregunta en caso de no tenerlos.
     */
    private fun getLocationPermission(){
        if (ActivityCompat
                        .checkSelfPermission(
                                context, fineLocationPermission
                        ) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true
            getLocation()
        }else{
            askPermissions()
        }
    }

    /**
     * Función que nos devuelve la localización actual del dispositivo.
     */
    @SuppressLint("MissingPermission")
    fun getLocation(){
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            myLocation = LatLng(location.latitude, location.longitude)
            googleMap.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true
            /* Creamos un String para hacer la solicitud a la API de Google Maps */
            val origin = "origin=" + myLocation.latitude + "," + myLocation.longitude + "&"
            val destination = "destination=" +
                    camera?.latitude +
                    "," + camera?.longitude + "&"
            val parameters = origin +
                    destination +
                    "sensor=false&mode=driving&key=" +
                    getApplication<Application>().getString(R.string.google_maps_key)
            loadURL("https://maps.googleapis.com/maps/api/directions/json?$parameters")
        }

    }

    /**
     * Función que hace la llamada a la API de Google Maps.
     *
     * @param url Url que queremos cargar.
     */
    private fun loadURL(url: String){
        val queue = Volley.newRequestQueue(context)
        val request = StringRequest(Request.Method.GET, url, { response ->
            /* Con la respuesta de la API, obtenemos las coordenadas */
            val coordinates = getCoordinates(response)
            /* Añadimos las coordenadas al Mapa */
            polyline = googleMap.addPolyline(coordinates)
        }, {})
        queue.add(request)
    }

    /**
     * Función que obtiene las coordenadas para mostrar la ruta hasta la cámara a través
     * de la respuesta de la API.
     *
     * @param response Respuesta de la API.
     * @return Las coordenadas del Polyline para añadir al mapa.
     */
    private fun getCoordinates(response: String): PolylineOptions{
        /* Utilizamos la clase Response para obtener los datos */
        val obj = Gson().fromJson(response, Response::class.java)
        val steps = obj.routes?.get(0)!!.legs?.get(0)!!.steps
        val coordinates = PolylineOptions()
        for(step in steps!!){
            /* Decodificamos las coordenadas del Polyline */
            decodePoly(step.polyline?.points.toString(), coordinates)
        }
        /* Le asignamos un color a la ruta */
        coordinates.color(getApplication<Application>().getColor(R.color.indigo_900_dark)).width(15f)
        return coordinates
    }

    /**
     * Función que activa el proceso solicitar permisos.
     */
    private fun askPermissions(){
        _askPermissions.value = true
        askPermissionsComplete()
    }

    /**
     * Función que certifica que el proceso de solicitar permisos se ha completado.
     */
    private fun askPermissionsComplete(){
        _askPermissions.value = null
    }

}