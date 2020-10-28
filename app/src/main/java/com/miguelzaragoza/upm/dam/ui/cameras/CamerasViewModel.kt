package com.miguelzaragoza.upm.dam.ui.cameras

import android.app.Application
import android.util.Xml
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miguelzaragoza.upm.dam.model.Camera
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class CamerasViewModel(application: Application): AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _camera = MutableLiveData<Camera>()
    val camera: LiveData<Camera>
        get() = _camera

    private val _cameras = MutableLiveData<List<Camera>>()
    val cameras: LiveData<List<Camera>>
        get() = _cameras

    init {
        getCameras()
    }

    private fun getCameras(){
        var coordinates = ""
        var url = ""
        var name = ""
        viewModelScope.launch {
            var list = ArrayList<Camera>()
            try{
                val parser = Xml.newPullParser()
                val inputStream = context.assets.open("CCTV.kml")
                parser.setInput(inputStream, null)
                var typeEvent: Int
                do{
                    typeEvent = parser.eventType
                    if(typeEvent == XmlPullParser.START_TAG){
                        var tagName = parser.name
                        when(tagName){
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
                _cameras.value = list
            }catch (e: IOException){
                e.printStackTrace()
            }catch (e: XmlPullParserException){
                e.printStackTrace()
            }
        }
    }

    fun displayImageCamera(camera: Camera){
        _camera.value = camera
    }

}