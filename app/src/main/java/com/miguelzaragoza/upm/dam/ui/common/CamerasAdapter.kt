package com.miguelzaragoza.upm.dam.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miguelzaragoza.upm.dam.databinding.ListViewItemBinding
import com.miguelzaragoza.upm.dam.model.Camera

/**
 * Adaptador del RecyclerView de las cámaras.
 * @param onClickListener: le pasamos la interfaz para gestionar los clicks de la lista
 */
class CamerasAdapter(private val onClickListener: OnClickListener): ListAdapter<Camera, CamerasViewHolder>(CamerasDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CamerasViewHolder {
        return CamerasViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CamerasViewHolder, position: Int) {
        val camera = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(camera)
        }
        holder.bind(camera)
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
    fun bind(camera: Camera) {
        binding.camera = camera
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
        return oldItem.status != newItem.status
    }
    override fun areContentsTheSame(oldItem: Camera, newItem: Camera): Boolean {
        return oldItem != newItem
    }
}

/**
 * Interfaz para gestionar los clicks del RecyclerView.
 * Lo utilizaremos para guardar el valor de la cámara seleccionada.
 */
class OnClickListener(val clickListener: (camera: Camera) -> Unit){
    fun onClick(camera: Camera) = clickListener(camera)
}