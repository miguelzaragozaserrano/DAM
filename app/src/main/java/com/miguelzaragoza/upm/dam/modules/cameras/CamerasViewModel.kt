package com.miguelzaragoza.upm.dam.modules.cameras

import android.app.Application
import android.util.Xml
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.model.Cameras
import com.miguelzaragoza.upm.dam.modules.base.BaseViewModel
import com.miguelzaragoza.upm.dam.modules.common.CamerasAdapter
import com.miguelzaragoza.upm.dam.modules.common.OnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

/**
 * ViewModel que realizará las funciones lógicas y almacenará los datos del
 * CamerasFragment.
 * @param application: objeto Application que nos permitirá obtener el contexto de la aplicación
 */
class CamerasViewModel(application: Application): BaseViewModel(application) {

    /******************************** VARIABLES BÁSICAS ********************************/
    /* Variables privadas para definir el contexto cuando sea necesario,
    *  y para la ejecución de hilos en segundo plano */
    private val context = application.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    /* Variable pública que crea el objeto CameraAdaper
    *  y guarda el valor de la cámara seleccioanda */
    var adapter = CamerasAdapter(OnClickListener { camera ->
        selectedCamera(camera)
    })

    /* Variables para guardar la lista que se comparte en el mapa
    *  y la lista del primer fragmento */
    var sharedList = Cameras()
    private var list = Cameras()

    /* Variable que nos ayuda con el número de cámaras que queremos mostrar en el mapa */
    private var singleMode: Boolean = false

    /* Variable de la última cámara seleccionada */
    private var lastCamera: Camera? = null

    /* Variables privadas para la lectura del fichero KML */
    private lateinit var inputStream: InputStream
    private lateinit var coordinates: String
    private lateinit var url: String
    private lateinit var name: String
    private lateinit var parser: XmlPullParser
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    /***************************** VARIABLES ENCAPSULADAS *****************************
     Nos permiten modificar su valor desde el ViewModel pero no desde una clase externa
     **********************************************************************************/

    /* Variable de la cámara actual */
    private val _camera = MutableLiveData<Camera>()
    val camera: LiveData<Camera>
        get() = _camera

    /* Variable de la lista de cámaras */
    private val _cameras = MutableLiveData<List<Camera>>()
    val cameras: LiveData<List<Camera>>
        get() = _cameras

    /* Variable para controlar la navegación al mapa */
    private val _navigateToSelectedCamera = MutableLiveData<Boolean>()
    val navigateToSelectedCamera: LiveData<Boolean>
        get() = _navigateToSelectedCamera

    /********************************* BLOQUE INICIAL *********************************/
    init {
        /* Utilizamos un hilo secundario para realizar el setup */
        coroutineScope.launch {
            initialSetup()
        }
    }

    /*************************** FUNCIONES PRIVADAS BÁSICAS ***************************/
    /**
     * Función suspendida que ejecuta a su vez dos hilos secundarios.
     */
    private suspend fun initialSetup(){
        /* Hilo que abrirá y recorrerá el KML */
        withContext(Dispatchers.IO){
            openFile()
            getCameras()
        }
        /* Inicializamos las variables de la última cámara seleccionada
        *  y de la lista de cámaras */
        _cameras.value = list
    }

    /*************************** FUNCIONES PRIVADAS ADAPTER ***************************/
    /**
     * Función que ejecuta un hilo secundario.
     * @param camera: cámara seleccionada
     */
    private fun selectedCamera(camera: Camera){
        coroutineScope.launch {
            displayCheck(camera)
        }
    }

    /**
     * Función suspendida que actualiza el status
     * de la cámara para activar o desactivar el RadioButton.
     * @param camera: cámara a la que queremos activar el tick
     */
    private suspend fun displayCheck(camera: Camera){
        withContext(Dispatchers.Main){
            /* Si no existe una última cámara seleccionada es porque
            *  es la primera vez que hacemos click en una, asi que le asignamos la clickeada */
            if(lastCamera == null) lastCamera = camera
            /* Por el contrario, si ya existe una, le asignamos la actual y
            *  marcamos que ya no está clickeada */
            else{
                lastCamera = _camera.value
                lastCamera?.status = false
            }
            /* Si el estado de la cámara clickeada es falso, lo marcamos como clickeada
            *  y actualizamos la cámara actual */
            if(!camera.status){
                camera.status = true
                _camera.value = camera
            }
            adapter.notifyDataSetChanged()
        }
    }

    /***************************** FUNCIONES PRIVADAS KML *****************************/
    /**
     * Función que se encarga de abrir el fichero KML.
     */
    private fun openFile() {
        inputStream = context.assets.open("CCTV.kml")
    }

    /**
     * Función suspendida que recorre el fichero KML para obtener los datos que necesitamos.
     */
    private suspend fun getCameras(){
        try{
            parser = Xml.newPullParser()
            parser.setInput(inputStream, null)
            var typeEvent: Int
            /* Repetimos el bucle hasta el final del documento */
            do{
                typeEvent = parser.eventType
                if(typeEvent == XmlPullParser.START_TAG){
                    /* Analizamos el nombre de la etiqueta */
                    when(parser.name){
                        /* Si es "Data" ... */
                        "Data" -> {
                            /* ... comprobamos que el primer atributo sea el nombre de la cámara */
                            if(parser.getAttributeValue(0) == "Nombre"){
                                /* Ejecutamos un hilo independiente del secundario para obtener la siguiente etiqueta */
                                withContext(Dispatchers.IO){
                                    getNextTag()
                                }
                                /* Ejecutamos un hilo indpendiente del secundario para obtener
                                *  el siguiente texto y guardarlo en la variable name */
                                withContext(Dispatchers.IO){
                                    name = getNextText()
                                }
                            }
                        }
                        /* Si es "description" ... */
                        "description" -> {
                            /* Ejecutamos un hilo independiente del secundario para obtener
                            *  el siguiente texto, quedarnos solamente con la URL y
                            *  guardarlo en la variable url */
                            withContext(Dispatchers.IO){
                                url = getNextText()
                                        .substringAfter("src=")
                                        .substringBefore("  width")
                            }
                        }
                        /* Si es "coordinates" ... */
                        "coordinates" -> {
                            /* Ejecutamos un hio independiente del secundario para obtener
                            *  el siguiente texto y guardarlo en la variable coordinates */
                            withContext(Dispatchers.IO){
                                coordinates = getNextText().substringBefore(",10")
                                latitude = coordinates.substringAfter(",").toDouble()
                                longitude = coordinates.substringBefore(",").toDouble()
                            }
                            /* Como las coordenadas son el último valor que se obtiene
                            *  de cada cámara, creamos un objeto Camera y lo añadimos a la lista
                            *  de cámaras */
                            list.add(Camera(name, url, LatLng(latitude, longitude), false))
                            sharedList.add(Camera(name, url, LatLng(latitude, longitude), false))
                        }
                    }
                }
                /* Ejecutamos un hilo independiente del secundario para
                *  obtener el siguiente tipo de evento */
                withContext(Dispatchers.IO){
                    typeEvent = getNext()
                }
            }while(typeEvent != XmlPullParser.END_DOCUMENT)
        }catch (e: IOException){
            /* Capturamos las posibles excepciones */
            e.printStackTrace()
        }catch (e: XmlPullParserException){
            /* Capturamos las posibles excepciones */
            e.printStackTrace()
        }
    }

    /************************ FUNCIONES PRIVADAS DE getCameras() ************************
     *** Se ejecutarán en un hilo diferente para evitar bloqueos del hilo secundario ***
     ***********************************************************************************/
    /**
     * Función que nos devuelve el siguiente tipo de evento.
     */
    private fun getNext(): Int = parser.next()
    /**
     * Función que nos devuelve la siguiente etiqueta.
     */
    private fun getNextTag(): Int = parser.nextTag()
    /**
     * Función que nos devuelve el siguiente texto.
     */
    private fun getNextText(): String = parser.nextText()


    /***************************** FUNCIONES PRIVADAS NAV *****************************/
    /**
     * Función que se llama desde el XML para activar el proceso de navegación al mapa.
     */
    fun showMap(){
        _navigateToSelectedCamera.value = true
        showMapComplete()
    }

    /**
     * Función que finaliza el proceso de navegación.
     */
    private fun showMapComplete(){
        _navigateToSelectedCamera.value = null
    }

    /***************************** FUNCIONES PARA EL MENÚ *****************************/
    /**
     * Función que ordena la lista de cámaras alfabéticamente en orden ascendente.
     */
    fun getAscendingList(){
        _cameras.value = cameras.value?.sortedBy {camera ->
            camera.name
        }
    }

    /**
     * Función que ordena la lista de cámaras alfabéticamente en orden descendente.
     */
    fun getDescendingList(){
        _cameras.value = cameras.value?.sortedByDescending {camera ->
            camera.name
        }
    }

    /**
     * Función que asigna la sharedList con el valor de la cámara seleccionada.
     */
    private fun getSingleCamera(){
        sharedList.clear()
        camera.value?.let { camera ->
            sharedList.add(camera)
        }
    }

    /**
     * Función que asigna la sharedList con el valor de todas las cámaras.
     */
    private fun getMultipleCameras(){
        sharedList.clear()
        for(camera in cameras.value!!){
            sharedList.add(camera)
        }
    }

    /**
     * Función, que dependiendo de las cámaras que quieres mostrar, llama a una función u otra.
     */
    fun setSharedList(){
        if(singleMode) getSingleCamera()
        else getMultipleCameras()
    }

    /**
     * Función que actualiza el valor del singleMode, para saber si queremos mostrar una
     * cámara o todas.
     * @param value: variable que determina si el valor del singleMode es true o false
     */
    fun setMode(value: Boolean){
        singleMode = value
    }

}