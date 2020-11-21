package com.miguelzaragoza.upm.dam.modules.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miguelzaragoza.upm.dam.binding.bindImage
import com.miguelzaragoza.upm.dam.database.CameraDao
import com.miguelzaragoza.upm.dam.databinding.ListViewItemBinding
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.modules.ui.cameras.FAV_MODE
import com.miguelzaragoza.upm.dam.modules.ui.cameras.NORMAL_MODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Adaptador del RecyclerView de las cámaras.
 *
 * @param database Base de datos.
 * @param onClickItem Detecta el click del elemento de la lista.
 * @param onClickFav Detecta el click del botón fav.
 */
class CamerasAdapter(
    private val database: CameraDao,
    private val onClickItem: OnClickListener,
    private val onClickFav: OnClickListener
): ListAdapter<Camera, CamerasViewHolder>(CamerasDiffCallback()){

    /******************************** VARIABLES BÁSICAS ********************************
     ***********************************************************************************/

    private var mode = NORMAL_MODE
    private var normalList = listOf<Camera>()
    private var favoriteList = listOf<Camera>()

    /******************************** FUNCIONES BÁSICAS ********************************
     ***********************************************************************************/

    /**
     * Función que detecta la lista leída del fichero KML.
     */
    fun setData(list : List<Camera>?) {
        if (list != null){
            if(normalList.isEmpty()){
                normalList = list
                submitList(list)
            }
        }
    }

    /**
     *  Función que filtra la lista por nombre dependiendo
     *  del modo en el que se encuentre el adaptador.
     *
     *  @param query Cadena para filtrar por el nombre.
     */
    fun filterByName(query: CharSequence?, ivCamera: ImageView, selectedCamera: Camera) {
        val list = mutableListOf<Camera>()
        if(mode == NORMAL_MODE){
            if(!query.isNullOrEmpty()) {
                list.addAll(normalList.filter { camera ->
                    camera.name.toLowerCase(
                        Locale.getDefault())
                        .contains(query.toString().toLowerCase(Locale.getDefault())
                        )
                })
            } else {
                list.addAll(normalList)
            }
        }
        if(mode == FAV_MODE) {
            if (!query.isNullOrEmpty()) {
                list.addAll(favoriteList.filter { camera ->
                    camera.name.toLowerCase(
                        Locale.getDefault())
                        .contains(query.toString().toLowerCase(Locale.getDefault()))
                })
            } else {
                list.addAll(favoriteList)
            }
        }
        if(!list.contains(selectedCamera)){
            ivCamera.setImageDrawable(null)
        }else{
            bindImage(ivCamera, selectedCamera.url)
        }
        submitList(list)
    }

    /**
     *  Función que ordena la lista normal alfabéticamente en orden ascendente.
     */
    fun filterAscending(){
        val list = mutableListOf<Camera>()
        list.addAll(normalList.sortedBy {camera ->
            camera.name
        })
        normalList = list
        submitList(list)
    }

    /**
     * Función que ordena la lista normal alfabéticamente en orden descendente.
     */
    fun filterDescending(){
        val list = mutableListOf<Camera>()
        list.addAll(normalList.sortedByDescending {camera ->
            camera.name
        })
        normalList = list
        submitList(list)
    }

    /**
     * Función que añade la cámara a la base de datos.
     *
     * @param camera Cámara que añadimos a la base de datos.
     */
    suspend fun addFavorite(camera: Camera){
        withContext(Dispatchers.IO){
            database.insert(camera)
        }
        if(camera.selected) database.updateSelected(false)
    }

    /**
     * Función que elimina la cámara de la base de datos.
     *
     * @param camera Cámara que eliminamos de la base de datos.
     */
    suspend fun removeFavorite(camera: Camera){
        withContext(Dispatchers.IO){
            database.remove(camera)
        }
        if(mode == FAV_MODE) showFavoriteList()
    }

    /**
     * Función que resetea la base de datos.
     */
    suspend fun resetFavoriteList(){
        withContext(Dispatchers.IO){
            database.clear()
        }
        favoriteList = listOf()
        submitList(favoriteList)
    }

    /**
     * Función que muestra la lista de favoritos.
     */
    fun showFavoriteList(){
        val list = mutableListOf<Camera>()
        list.addAll(normalList.filter { camera ->
            camera.fav
        })
        favoriteList = list
        submitList(favoriteList)
    }

    /**
     * Función que muestra la lista normal.
     */
    fun showNormalList(){
        submitList(normalList)
    }

    /**
     * Función que cambia el modo.
     *
     * @param type Tipo de modo.
     */
    fun setMode(type: Int){
       mode = type
    }

    /******************************** FUNCIONES OVERRIDE *******************************
     ***********************************************************************************/

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CamerasViewHolder {
        return CamerasViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CamerasViewHolder, position: Int) {
        val camera = getItem(position)
        holder.itemView.setOnClickListener {
            onClickItem.onClick(camera)
        }
        holder.bind(camera, onClickFav)
    }
}

/**
 * Clase CamerasViewHolder que utilizamos para asignar enlazar objetos e inflar el RecyclerView.
 *
 * @param binding Binding vinculado al fichero list_view_item.xml
 */
class CamerasViewHolder private constructor(private val binding: ListViewItemBinding):
    RecyclerView.ViewHolder(binding.root){
    fun bind(camera: Camera, onClickFav: OnClickListener) {
        binding.camera = camera
        binding.favButton.setOnClickListener {
            onClickFav.onClick(camera)
        }
        binding.executePendingBindings()
    }
    companion object {
        fun from(parent: ViewGroup): CamerasViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding =
                ListViewItemBinding.inflate(layoutInflater, parent, false)
            return CamerasViewHolder(binding)
        }
    }
}

/**
 *  Clase DiffUtil que nos permite no pintar la lista entera de nuevo
 */
class CamerasDiffCallback: DiffUtil.ItemCallback<Camera>(){
    override fun areItemsTheSame(oldItem: Camera, newItem: Camera): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: Camera, newItem: Camera): Boolean {
        return oldItem.selected == newItem.selected && oldItem.fav == newItem.fav
    }
}

/**
 * Interfaz para gestionar los clicks del RecyclerView.
 */
class OnClickListener(val clickListener: (camera: Camera) -> Unit){
    fun onClick(camera: Camera) = clickListener(camera)
}