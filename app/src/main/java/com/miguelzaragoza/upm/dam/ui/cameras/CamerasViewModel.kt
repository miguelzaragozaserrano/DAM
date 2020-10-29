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

class CamerasViewModel(application: Application): AndroidViewModel(application) {

    private val context = application.applicationContext

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    var adapter = CamerasAdapter(OnClickListener { camera ->
        displayCheck(camera)
    })

    private var list = ArrayList<Camera>()
    private lateinit var inputStream: InputStream
    private lateinit var coordinates: String
    private lateinit var url: String
    private lateinit var name: String
    private lateinit var parser: XmlPullParser

    private val _camera = MutableLiveData<Camera>()
    val camera: LiveData<Camera>
        get() = _camera

    private val _lastCamera = MutableLiveData<Camera>()
    private val lastCamera: LiveData<Camera>
        get() = _lastCamera

    private val _cameras = MutableLiveData<List<Camera>>()
    val cameras: LiveData<List<Camera>>
        get() = _cameras

    init {
        coroutineScope.launch {
            setupCameras()
        }
    }

    private suspend fun setupCameras(){
        withContext(Dispatchers.IO){
            openFile()
            getCameras()
        }
        withContext(Dispatchers.Main){
            _lastCamera.value = null
            _cameras.value = list
        }
    }

    private fun openFile() {
        inputStream = context.assets.open("CCTV.kml")
    }

    private fun getNext(): Int = parser.next()
    private fun getNextTag(): Int = parser.nextTag()
    private fun getNextText(): String = parser.nextText()

    private suspend fun getCameras(){
        try{
            parser = Xml.newPullParser()
            parser.setInput(inputStream, null)
            var typeEvent: Int
            do{
                typeEvent = parser.eventType
                if(typeEvent == XmlPullParser.START_TAG){
                    when(parser.name){
                        "Data" -> {
                            if(parser.getAttributeValue(0) == "Nombre"){
                                withContext(Dispatchers.Default){
                                    getNextTag()
                                }
                                withContext(Dispatchers.Default){
                                    name = getNextText()
                                }
                            }
                        }
                        "description" -> {
                            withContext(Dispatchers.Default){
                                url = getNextText().substringAfter("src=").substringBefore("  width")
                            }
                        }
                        "coordinates" -> {
                            withContext(Dispatchers.Default){
                                coordinates = getNextText()
                            }
                            list.add(Camera(name, url, coordinates, false))
                        }
                    }
                }
                withContext(Dispatchers.Default){
                    typeEvent = getNext()
                }
            }while(typeEvent != XmlPullParser.END_DOCUMENT)
        }catch (e: IOException){
            e.printStackTrace()
        }catch (e: XmlPullParserException){
            e.printStackTrace()
        }
    }

    fun displayCheck(camera: Camera){
        coroutineScope.launch {
            if(lastCamera.value == null) _lastCamera.value = camera
            else{
                _lastCamera.value = _camera.value
                _lastCamera.value?.status = false
            }
            if(!camera.status){
                camera.status = true
                _camera.value = camera
            }
        }
    }

}