package com.miguelzaragoza.upm.dam.ui.common

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.miguelzaragoza.upm.dam.databinding.ListViewItemBinding
import com.miguelzaragoza.upm.dam.model.Camera

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

class CamerasViewHolder private constructor(val binding: ListViewItemBinding): RecyclerView.ViewHolder(binding.root){
    fun bind(item: Camera) {
        binding.camera = item
        binding.executePendingBindings()
    }
    companion object {
        fun from(parent: ViewGroup): CamerasViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = ListViewItemBinding.inflate(layoutInflater, parent, false)
            return CamerasViewHolder(binding)
        }
    }
}

class CamerasDiffCallback: DiffUtil.ItemCallback<Camera>(){
    override fun areItemsTheSame(oldItem: Camera, newItem: Camera): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Camera, newItem: Camera): Boolean {
        return oldItem == newItem
    }
}

class OnClickListener(val clickListener: (camera: Camera) -> Unit){
    fun onClick(camera: Camera) = clickListener(camera)
}