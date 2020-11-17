package com.miguelzaragoza.upm.dam.modules.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
 * @param onClickListener le pasamos la interfaz para gestionar los clicks de la lista
 */
class CamerasAdapter(
    private val database: CameraDao,
    private val onClickItem: OnClickListener,
    private val onClickFav: OnClickListener
): ListAdapter<Camera, CamerasViewHolder>(CamerasDiffCallback()){

    private var mode = NORMAL_MODE
    private var normalList = listOf<Camera>()
    private var favoriteList = listOf<Camera>()

    fun setData(list : List<Camera>?) {
        if (list != null){
            if(normalList.isEmpty()){
                normalList = list
                submitList(list)
            }
        }
    }

    fun filterByName(query: CharSequence?) {
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
                    camera.name.toLowerCase(Locale.getDefault()).contains(query.toString().toLowerCase(Locale.getDefault()))
                })
            } else {
                list.addAll(favoriteList)
            }
        }
        submitList(list)
    }

    fun filterAscending(){
        val list = mutableListOf<Camera>()
        list.addAll(normalList.sortedBy {camera ->
            camera.name
        })
        normalList = list
        submitList(list)
    }

    fun filterDescending(){
        val list = mutableListOf<Camera>()
        list.addAll(normalList.sortedByDescending {camera ->
            camera.name
        })
        normalList = list
        submitList(list)
    }

    suspend fun addFavorite(camera: Camera){
        withContext(Dispatchers.IO){
            database.insert(camera)
        }
        if(camera.selected) database.updateSelected(false)
    }
    
    suspend fun removeFavorite(camera: Camera){
        withContext(Dispatchers.IO){
            database.remove(camera)
        }
        if(mode == FAV_MODE) submitList(favoriteList)
    }

    suspend fun resetFavoriteList(){
        withContext(Dispatchers.IO){
            database.clear()
        }
        favoriteList = listOf()
        submitList(favoriteList)
    }

    fun showFavoriteList(){
        val list = mutableListOf<Camera>()
        list.addAll(normalList.filter { camera ->
            camera.fav
        })
        favoriteList = list
        submitList(favoriteList)
    }

    fun showNormalList(){
        submitList(normalList)
    }

    fun setMode(type: Int){
       mode = type
    }

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
 * @param binding: binding vinculado al fichero list_view_item.xml
 */
class CamerasViewHolder private constructor(private val binding: ListViewItemBinding): RecyclerView.ViewHolder(binding.root){
    /**
     * Función para enlazar los objetos del XML.
     * @param camera: cámara seleccionada para enlazarla con la del XML
     */
    fun bind(camera: Camera, onClickFav: OnClickListener) {
        binding.camera = camera
        binding.favButton.setOnClickListener {
            onClickFav.onClick(camera)
        }
        binding.executePendingBindings()
    }
    companion object {
        /**
         * Funcion para inflar el RecyclerView.
         * @param parent: vista padre
         */
        fun from(parent: ViewGroup): CamerasViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ListViewItemBinding.inflate(layoutInflater, parent, false)
            return CamerasViewHolder(binding)
        }
    }
}

/* ???? */
class CamerasDiffCallback: DiffUtil.ItemCallback<Camera>(){
    override fun areItemsTheSame(oldItem: Camera, newItem: Camera): Boolean {
        return oldItem.id == newItem.id
    }
    override fun areContentsTheSame(oldItem: Camera, newItem: Camera): Boolean {
        return oldItem == newItem
    }
}

/**
 * Interfaz para gestionar los clicks del RecyclerView.
 * Lo utilizaremos para guardar el valor de la cámara seleccionada.
 * @param clickListener: de
 */
class OnClickListener(val clickListener: (camera: Camera) -> Unit){
    fun onClick(camera: Camera) = clickListener(camera)
}