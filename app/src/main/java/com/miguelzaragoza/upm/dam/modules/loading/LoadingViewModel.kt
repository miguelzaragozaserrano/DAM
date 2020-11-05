package com.miguelzaragoza.upm.dam.modules.loading

import android.animation.ValueAnimator
import android.app.Application
import android.os.CountDownTimer
import android.util.Xml
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.model.Cameras
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
 * LoadingFragment.
 * @param application: variable que nos permitirá obtener el contexto de la aplicación
 */
class LoadingViewModel(application: Application): AndroidViewModel(application) {

    /******************************** VARIABLES BÁSICAS ********************************/
    /* Variables privadas para definir el contexto cuando sea necesario,
    *  y para la ejecución de hilos en segundo plano */
    private val context = application.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    /* Variables privadas para la lectura del fichero KML */
    private lateinit var inputStream: InputStream
    private lateinit var coordinates: String
    private lateinit var url: String
    private lateinit var name: String
    private lateinit var parser: XmlPullParser
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    /* Variable para guardar la lista de cámaras */
    var list = Cameras()

    /* Variables para los ProgressBar */
    var millisUntilFinished: Int = 0
    val animator: ValueAnimator = ValueAnimator.ofInt(0, 100)
    private var countDownTimer = object: CountDownTimer(3000, 1){
        /**
         * Función que se carga con cada countDownInterval.
         * @param p0: tiempo que queda para que se termine
         */
        override fun onTick(p0: Long) {
            millisUntilFinished = (p0 / 100 - 100).toInt()
            increaseBar()
        }
        /**
         * Funcion que se ejecuta cuando finaliza el tiempo.
         */
        override fun onFinish() {
            showList()
        }
    }

    /***************************** VARIABLES ENCAPSULADAS *****************************
     Nos permiten modificar su valor desde el ViewModel pero no desde una clase externa
     **********************************************************************************/
    /* Variable para controlar la navegación al siguiente fragmento */
    private val _navigateToCamerasFragment = MutableLiveData<Boolean>()
    val navigateToCamerasFragment: LiveData<Boolean>
        get() = _navigateToCamerasFragment

    /* Variable para controlar el aumento del ProgressBar Horizontal */
    private val _increaseProgressBar = MutableLiveData<Boolean>()
    val increaseProgressBar: LiveData<Boolean>
        get() = _increaseProgressBar


    /********************************* BLOQUE INICIAL *********************************/
    init {
        /* Lanzamos una CoroutineScope para analizar hilos secundarios */
        coroutineScope.launch {
            initialSetup()
        }
    }

    /*************************** FUNCIONES PRIVADAS BÁSICAS ***************************/
    /**
     * Función suspendida que ejecuta a su vez tres hilos secundarios.
     */
    private suspend fun initialSetup(){
        /* Hilo que abrirá y recorrerá el KML */
        withContext(Dispatchers.IO){
            openFile()
            getCameras()
        }
        /* Hilo que arranca el objeto ValueAnimator */
        withContext(Dispatchers.Main){
            animator.start()
        }
        /* Hilo que arranaca el objeto CountDownTimer */
        withContext(Dispatchers.IO){
            countDownTimer.start()
        }
    }

    /**
     * Función que activa el proceso de incrementar el ProgressBar.
     */
    private fun increaseBar(){
        _increaseProgressBar.value = true
        increaseBarComplete()
    }
    /**
     * Función que certifica que el proceso de incrementar se ha completado.
     */
    private fun increaseBarComplete(){
        _increaseProgressBar.value = null
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
            /* Iniciamos uans variables necesarias en el bucle */
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
                                /* Ejecutamos un hilo independiente al secundario para obtener la siguiente etiqueta */
                                withContext(Dispatchers.IO){
                                    getNextTag()
                                }
                                /* Ejecutamos un hilo indpendiente al secundario para obtener
                                *  el siguiente texto y guardarlo en la variable name */
                                withContext(Dispatchers.IO){
                                    name = getNextText()
                                }
                            }
                        }
                        /* Si es "description" ... */
                        "description" -> {
                            /* Ejecutamos un hilo independiente al secundario para obtener
                            *  el siguiente texto y guardarlo en la variable url */
                            withContext(Dispatchers.IO){
                                url = getNextText()
                                    .substringAfter("src=")
                                    .substringBefore("  width")
                            }
                        }
                        /* Si es "coordinates" ... */
                        "coordinates" -> {
                            /* Ejecutamos un hio independiente al secundario para obtener
                            *  el siguiente texto y guardarlo en la variable coordinates */
                            withContext(Dispatchers.IO){
                                coordinates = getNextText().substringBefore(",10")
                                /* De esa variable coordinates, obtenemos la latitud y longitud
                                *  y las guardamos en sus respectivas variables */
                                latitude = coordinates.substringAfter(",").toDouble()
                                longitude = coordinates.substringBefore(",").toDouble()
                            }
                            /* Como las coordenadas son el último valor que se obtiene
                            *  de cada cámara, creamos un objeto Camera y lo añadimos a la lista
                            *  de cámaras */
                            list.add(Camera(list.size + 1, name, url, LatLng(latitude, longitude), false))
                        }
                    }
                }
                /* Ejecutamos un hilo independiente al secundario para
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

    /************************* FUNCIONES PRIVADAS getCameras() *************************
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

    /************************** FUNCIONES PRIVADAS NAVEGACIÓN *************************/
    /**
     * Función que se llama para activar el proceso de navegación a CamerasFragment.
     */
    private fun showList(){
        _navigateToCamerasFragment.value = true
        showListComplete()
    }
    /**
     * Función que certifica que el proceso de navegación se ha completado.
     */
    private fun showListComplete(){
        _navigateToCamerasFragment.value = null
    }

 }