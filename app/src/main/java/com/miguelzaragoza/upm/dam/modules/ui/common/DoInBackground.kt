package com.miguelzaragoza.upm.dam.modules.ui.common

import android.annotation.SuppressLint
import android.app.Application
import android.os.SystemClock
import android.util.Log
import android.util.Xml
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.miguelzaragoza.upm.dam.R
import com.miguelzaragoza.upm.dam.database.CameraDatabase
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.model.Cameras
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

@SuppressLint("SetTextI18n")
class DoInBackground(
        private var progressBar: ProgressBar,
        val application: Application,
        private var textLoading: TextView){

    /******************************** VARIABLES BÁSICAS ********************************
     ***********************************************************************************/

    /* Variable para definir la base de datos */
    private val database = CameraDatabase.getInstance(application).cameraDao

    /* Variables privadas para la lectura del fichero KML */
    private lateinit var inputStream: InputStream
    private lateinit var coordinates: String
    private lateinit var url: String
    private lateinit var name: String
    private lateinit var parser: XmlPullParser
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    /********************************* COMPANION OBJECT *******************************
     **********************************************************************************/

    companion object{
        var list = Cameras()

        private val _navigateToCamerasFragment = MutableLiveData<Boolean>()
        val navigateToCamerasFragment: LiveData<Boolean>
            get() = _navigateToCamerasFragment

        fun showListComplete(){
            _navigateToCamerasFragment.value = null
        }
    }

    /********************************* BLOQUE INICIAL *********************************
     **********************************************************************************/

    init {
        doAsync {
            var i = 0
            progressBar.progress = 0
            while (i <= 100) {
                progressBar.progress = i
                if(i == 0){
                    uiThread{
                        textLoading.text = application.applicationContext.getString(R.string.loading_opening)
                    }
                    openFile()
                }
                if(i == 25){
                    uiThread{
                        textLoading.text = application.applicationContext.getString(R.string.loading_reading)
                    }
                    getCameras()
                }
                if(i == 75){
                    uiThread{
                        textLoading.text = application.applicationContext.getString(R.string.loading_getting)
                    }
                }
                SystemClock.sleep(1000)
                i += 25
            }
            uiThread{
                showList()
            }
        }
    }

    /***************************** FUNCIONES PRIVADAS KML *****************************
     **********************************************************************************/

    /**
     * Función que se encarga de abrir el fichero KML.
     */
    private fun openFile() {
        inputStream = application.applicationContext.assets.open("CCTV.kml")

    }

    /**
     * Función que recorre el fichero KML para obtener los datos que necesitamos.
     */
    private fun getCameras(){
        try{
            /* Iniciamos uans variables necesarias en el bucle */
            parser = Xml.newPullParser()
            parser.setInput(inputStream, null)
            var typeEvent: Int
            /* Repetimos el bucle hasta el final del documento */
            do{
                Log.d("HOLa", "getCameras")
                typeEvent = parser.eventType
                if(typeEvent == XmlPullParser.START_TAG){
                    /* Analizamos el nombre de la etiqueta */
                    when(parser.name){
                        /* Si es "Data" ... */
                        "Data" -> {
                            /* ... comprobamos que el primer atributo sea el nombre de la cámara */
                            if(parser.getAttributeValue(0) == "Nombre"){
                                /* Ejecutamos un hilo independiente al secundario
                                *  para obtener la siguiente etiqueta */
                                getNextTag()

                                /* Ejecutamos un hilo indpendiente al secundario para obtener
                                *  el siguiente texto y guardarlo en la variable name */

                                name = getNextText()

                            }
                        }
                        /* Si es "description" ... */
                        "description" -> {
                            /* Ejecutamos un hilo independiente al secundario para obtener
                            *  el siguiente texto y guardarlo en la variable url */

                            url = getNextText()
                                .substringAfter("src=")
                                .substringBefore("  width")

                        }
                        /* Si es "coordinates" ... */
                        "coordinates" -> {
                            Log.d("HOLa", "coordinates")
                            /* Ejecutamos un hio independiente al secundario para obtener
                            *  el siguiente texto y guardarlo en la variable coordinates */

                            coordinates = getNextText().substringBefore(",10")
                            /* De esa variable coordinates, obtenemos la latitud y longitud
                            *  y las guardamos en sus respectivas variables */
                            latitude = coordinates.substringAfter(",").toDouble()
                            longitude = coordinates.substringBefore(",").toDouble()

                            /* Como las coordenadas son el último valor que se obtiene
                            *  de cada cámara, creamos un objeto Camera y lo añadimos a la lista
                            *  de cámaras. */
                            /* Para ello, comprobamos si existe la cámara en la base de datos
                            *  y en caso de que exista, en la lista tendremos que marcar
                            *  el atributo fav como true */
                            val camera = database.get(list.size + 1)
                            var fav = false
                            if(camera != null) fav = camera.fav
                            list.add(
                                Camera(list.size + 1,
                                    name,
                                    url,
                                    latitude,
                                    longitude,
                                    selected = false,
                                    fav = fav
                                )
                            )
                        }
                    }
                }
                /* Ejecutamos un hilo independiente al secundario para
                *  obtener el siguiente tipo de evento */

                typeEvent = getNext()

            }while(typeEvent != XmlPullParser.END_DOCUMENT)
        }catch (e: IOException){
            /* Capturamos las posibles excepciones */
            e.printStackTrace()
        }catch (e: XmlPullParserException){
            /* Capturamos las posibles excepciones */
            e.printStackTrace()
        }
    }

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

    /**
     * Función que se llama para activar el proceso de navegación a CamerasFragment.
     */
    private fun showList(){
        _navigateToCamerasFragment.value = true
    }

}