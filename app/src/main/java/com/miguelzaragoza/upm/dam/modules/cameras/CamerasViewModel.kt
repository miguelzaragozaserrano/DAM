package com.miguelzaragoza.upm.dam.modules.cameras

import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.model.Cameras
import com.miguelzaragoza.upm.dam.modules.common.CamerasAdapter
import com.miguelzaragoza.upm.dam.modules.common.OnClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel que realizará las funciones lógicas y almacenará los datos del
 * CamerasFragment.
 */
class CamerasViewModel: ViewModel() {

    /******************************** VARIABLES BÁSICAS ********************************
     ***********************************************************************************/

    /* Variable privada para la ejecución de hilos en segundo plano */
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    /* Variable pública que crea el objeto CameraAdaper
    *  y guarda el valor de la cámara seleccioanda */
    var adapter = CamerasAdapter(OnClickListener { camera ->
        selectedCamera(camera)
    })

    /* Variables para guardar la lista que se comparte en el mapa
    *  y la lista que se muestra en el propio fragmento */
    var sharedList = Cameras()
    var list = Cameras()

    /* Variable de la última cámara seleccionada */
    private var lastCamera: Camera? = null

    /* Variables que nos ayudan con las opciones del menú */
    var order: Int = NOT_ORDER
    var focus: Boolean = false
    var querySearched: String = ""
    var showAllCameras: Boolean = false
    lateinit var searchView: SearchView


    /***************************** VARIABLES ENCAPSULADAS *****************************
     **********************************************************************************/

    /* Variable de la cámara actual */
    private val _camera = MutableLiveData<Camera>()
    val camera: LiveData<Camera>
        get() = _camera

    /* Variable de la lista de cámaras */
    private val _cameras = MutableLiveData<List<Camera>>()
    val cameras: LiveData<List<Camera>>
        get() = _cameras

    /* Variable para controlar la navegación al siguiente fragmento */
    private val _navigateToSelectedCamera = MutableLiveData<Boolean>()
    val navigateToSelectedCamera: LiveData<Boolean>
        get() = _navigateToSelectedCamera

    /********************************* BLOQUE INICIAL *********************************
     **********************************************************************************/

    init {
        _cameras.value = list
    }

    /*************************** FUNCIONES PRIVADAS ADAPTER ***************************
     **********************************************************************************/

    /**
     * Función que ejecuta un hilo secundario.
     *
     * @param camera Cámara seleccionada.
     */
    private fun selectedCamera(camera: Camera){
        coroutineScope.launch {
            displayCheck(camera)
        }
    }

    /**
     * Función suspendida que actualiza el status
     * de la cámara para activar o desactivar el RadioButton.
     *
     * @param camera Cámara a la que queremos activar el check.
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
            /* Avisamos al adaptador de los cambios */
            adapter.notifyDataSetChanged()
        }
    }

    /***************************** FUNCIONES PRIVADAS NAV *****************************
     **********************************************************************************/

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

    /***************************** FUNCIONES PRIVADAS MENÚ *****************************
     ***********************************************************************************/

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
        for(camera in list){
            sharedList.add(camera)
        }
    }

    /**
     * Función, que dependiendo de las cámaras que quieres mostrar, llama a una función u otra.
     */
    fun setSharedList(){
        if(showAllCameras) getMultipleCameras()
        else getSingleCamera()
    }

    /**
     * Función que actualiza el valor de showAllCameras, para saber si queremos mostrar una
     * cámara o todas.
     *
     * @param value Variable que determina si el checkable de la opción "Todas" está
     * activado o no.
     */
    fun setMode(value: Boolean){
        showAllCameras = value
    }

}