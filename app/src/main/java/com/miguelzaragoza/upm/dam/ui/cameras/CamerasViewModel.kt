package com.miguelzaragoza.upm.dam.ui.cameras

import android.app.Application
import android.util.Xml
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.miguelzaragoza.upm.dam.model.Camera
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class CamerasViewModel(application: Application): AndroidViewModel(application) {

    private val context = application.applicationContext
    private val coroutineMain = CoroutineScope(Dispatchers.Main)

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
        coroutineMain.launch {
            getCameras()
            _lastCamera.value = null
        }
    }

    private suspend fun getCameras(){
        var coordinates: String
        var url = ""
        var name = ""
        val list = ArrayList<Camera>()
        withContext(Dispatchers.IO) {
            try{
                val parser = Xml.newPullParser()
                val inputStream = context.assets.open("CCTV.kml")
                parser.setInput(inputStream, null)
                var typeEvent: Int
                do{
                    typeEvent = parser.eventType
                    if(typeEvent == XmlPullParser.START_TAG){
                        when(parser.name){
                            "Data" -> {
                                if(parser.getAttributeValue(0) == "Nombre"){
                                    parser.nextTag()
                                    name = parser.nextText()
                                }
                            }
                            "description" -> {
                                url = parser.nextText().substringAfter("src=").substringBefore("  width")
                            }
                            "coordinates" -> {
                                coordinates = parser.nextText()
                                list.add(Camera(name, url, coordinates, false))
                            }
                        }
                    }
                    typeEvent = parser.next()
                }while(typeEvent != XmlPullParser.END_DOCUMENT)
            }catch (e: IOException){
                e.printStackTrace()
            }catch (e: XmlPullParserException){
                e.printStackTrace()
            }
        }
        _cameras.value = list
    }

    fun displayCheck(camera: Camera){
        coroutineMain.launch {
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