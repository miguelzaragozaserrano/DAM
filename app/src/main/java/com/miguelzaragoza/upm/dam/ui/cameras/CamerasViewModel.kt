package com.miguelzaragoza.upm.dam.ui.cameras

import android.app.Application
import android.util.Xml
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.ui.common.CamerasAdapter
import com.miguelzaragoza.upm.dam.ui.common.OnClickListener
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
 * ImageFragment y ListFragment.
 */
class CamerasViewModel(application: Application): AndroidViewModel(application) {

    /******************************** VARIABLES BÁSICAS ********************************/
    /* Variables privadas para definir el contexto cuando sea necesario,
    *  y para la ejecución de hilos en segundo plano */
    private val context = application.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    /* Variable pública que crea el objeto CameraAdaper*/
    var adapter = CamerasAdapter(OnClickListener { camera ->
        selectedCamera(camera)
    })

    /* Variables privadas para la lectura del fichero KML */
    private var list = ArrayList<Camera>()
    private lateinit var inputStream: InputStream
    private lateinit var coordinates: String
    private lateinit var url: String
    private lateinit var name: String
    private lateinit var parser: XmlPullParser

    /***************************** VARIABLES ENCAPSULADAS *****************************
     Nos permiten modificar su valor desde el ViewModel pero no desde una clase externa
     **********************************************************************************/
    /* Variable de la cámara actual */
    private val _camera = MutableLiveData<Camera>()
    val camera: LiveData<Camera>
        get() = _camera

    /* Variable de la última cámara seleccionada */
    private val _lastCamera = MutableLiveData<Camera>()
    private val lastCamera: LiveData<Camera>
        get() = _lastCamera

    /* Variable de la lista de cámaras */
    private val _cameras = MutableLiveData<List<Camera>>()
    val cameras: LiveData<List<Camera>>
        get() = _cameras

    /********************************* BLOQUE INICIAL *********************************/
    init {
        /* Utilizamos un hilo secundario para realizar el setup */
        coroutineScope.launch {
            initialSetup()
        }
    }

    /*************************** FUNCIONES PRIVADAS BÁSICAS ***************************/
    /* Función que ejecuta a su vez dos hilos secundarios */
    private suspend fun initialSetup(){
        /* Hilo que abrirá y recorrerá el KML */
        withContext(Dispatchers.IO){
            openFile()
            getCameras()
        }
        /* Hilo de que inicializa las variables de la última cámara seleccionada
        *  y de la lista de cámaras */
        withContext(Dispatchers.Main){
            _lastCamera.value = null
            _cameras.value = list
        }
    }

    /*************************** FUNCIONES PRIVADAS ADAPTER ***************************/
    /* Función que ejecuta un hilo secundario */
    private fun selectedCamera(camera: Camera){
        coroutineScope.launch {
            displayCheck(camera)
        }
    }

    /* Función que actualiza el status de la cámara para activar o desactivar el RadioButton */
    private suspend fun displayCheck(camera: Camera){
        withContext(Dispatchers.Main){
            /* Si no existe una última cámara seleccionada es porque
            *  es la primera vez que hacemos click en una asi que le asignamos la clickeada */
            if(lastCamera.value == null) _lastCamera.value = camera
            /* Por el contrario, si ya existe una, le asignamos la actual y
            *  marcamos que ya no está clickeada */
            else{
                _lastCamera.value = _camera.value
                _lastCamera.value?.status = false
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
    /* Función que se encarga de abrir el fichero KML */
    private fun openFile() {
        inputStream = context.assets.open("CCTV.kml")
    }

    /* Función que recorre el fichero KML para obtener los datos que necesitamos */
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
                                coordinates = getNextText()
                            }
                            /* Como las coordenadas son el último valor que se obtiene
                            *  de cada cámara, creamos un objeto Camera y lo añadimos a la lista
                            *  de cámaras */
                            list.add(Camera(name, url, coordinates, false))
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
    /* Función que nos devuelve el siguiente tipo de evento */
    private fun getNext(): Int = parser.next()
    /* Función que nos devuelve la siguiente etiqueta */
    private fun getNextTag(): Int = parser.nextTag()
    /* Función que nos devuelve el siguiente texto */
    private fun getNextText(): String = parser.nextText()

}