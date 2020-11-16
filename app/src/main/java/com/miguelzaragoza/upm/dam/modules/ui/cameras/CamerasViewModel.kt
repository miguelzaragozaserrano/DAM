package com.miguelzaragoza.upm.dam.modules.ui.cameras

import android.app.Application
import android.graphics.drawable.Drawable
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.miguelzaragoza.upm.dam.R
import com.miguelzaragoza.upm.dam.database.CameraDao
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.model.Cameras
import com.miguelzaragoza.upm.dam.modules.ui.common.CamerasAdapter
import com.miguelzaragoza.upm.dam.modules.ui.common.OnClickListener
import com.miguelzaragoza.upm.dam.modules.utils.hasConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel que realizará las funciones lógicas y almacenará los datos del
 * CamerasFragment.
 *
 * @param application Variable que nos permitirá obtener el contexto de la aplicación.
 * @param database Base de datos Room.
 */
class CamerasViewModel(application: Application, val database: CameraDao): AndroidViewModel(application) {

    /******************************** VARIABLES BÁSICAS ********************************
     ***********************************************************************************/

    /* Variables privadas para definir el contexto cuando sea necesario,
    *  y para la ejecución de hilos en segundo plano */
    private val context = application.applicationContext
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    /* Variable pública que crea el objeto CameraAdaper */
    var adapter = CamerasAdapter(database, OnClickListener { camera ->
        selectedCamera(camera)
    }, OnClickListener { camera ->
        setFavorite(camera)
    })

    /* Variables para guardar la lista que se muestra en el propio fragmento
    *  y la lista que se comparte en el mapa */
    var list = Cameras()
    var sharedList = Cameras()

    /* Variable de la última cámara seleccionada */
    private var lastCamera: Camera? = null

    /* Variables que nos ayudan con las opciones del menú */
    var mode: Int = NORMAL_MODE
    var order: Int = NOT_ORDER
    var focus: Boolean = false
    var cluster: Boolean = false
    var querySearched: String = ""
    var showAllCameras: Boolean = false
    lateinit var optionReset: MenuItem
    var iconFav: Drawable? = ContextCompat
            .getDrawable(context, R.drawable.ic_favorite_off)
    var iconOrder: Drawable? = ContextCompat
            .getDrawable(context, R.drawable.ic_ascending_order)

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
     * Función que añade o elimina la cámara de favoritos.
     */
    private fun setFavorite(camera: Camera){
        /* Cambiamos el parámetro fav de la cámara */
        camera.fav = !camera.fav
        /* En caso de ser favorita, la añadimos a Room */
        if(camera.fav){
            coroutineScope.launch {
                adapter.addFavorite(camera)
            }
            Toast.makeText(context, context.getString(R.string.toast_addfav), Toast.LENGTH_LONG).show()
        }
        /* En caso contrario, la eliminamos de Room */
        else{
            coroutineScope.launch {
                adapter.removeFavorite(camera)
            }
            Toast.makeText(context, context.getString(R.string.toast_remfav), Toast.LENGTH_LONG).show()
        }
        /* Avisamos al adaptador de los cambios */
        adapter.notifyDataSetChanged()
    }

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
                lastCamera?.selected = false
            }
            /* Si el estado de la cámara clickeada es falso, lo marcamos como clickeada
            *  y actualizamos la cámara actual */
            if(!camera.selected){
                camera.selected = true
                _camera.value = camera
            }
        }
        /* Avisamos al adaptador de los cambios */
        adapter.notifyDataSetChanged()
    }

    /***************************** FUNCIONES PRIVADAS NAV *****************************
     **********************************************************************************/

    /**
     * Función que se llama desde el XML para activar el proceso de navegación al mapa.
     */
    fun showMap(){
        /* Comprobamos si hay una cámara seleccionada */
        if(camera.value != null)
            /* Comprobamos si hay conexión a internet */
            if(hasConnection(getApplication<Application>())){
                _navigateToSelectedCamera.value = true
                showMapComplete()
            }else
                Toast
                    .makeText(context, context.getString(R.string.check_connection), Toast.LENGTH_LONG)
                    .show()
    }

    /**
     * Función que finaliza el proceso de navegación.
     */
    private fun showMapComplete(){
        _navigateToSelectedCamera.value = null
    }

    /********************************** FUNCIONES MENÚ *********************************
     ***********************************************************************************/

    /**
     * Función, que dependiendo de las cámaras que quieres mostrar, llama a una función u otra.
     */
    fun setSharedList(){
        if(showAllCameras) getMultipleCameras()
        else getSingleCamera()
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
        for(camera in list){
            sharedList.add(camera)
        }
    }

    /**
     * Función que resetea la lista de favoritos.
     */
    fun reset(){
        coroutineScope.launch {
            adapter.resetFavoriteList()
        }
    }

}