package com.miguelzaragoza.upm.dam.ui.cameras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.miguelzaragoza.upm.dam.databinding.FragmentCamerasBinding
import com.miguelzaragoza.upm.dam.ui.common.CamerasAdapter
import com.miguelzaragoza.upm.dam.ui.common.OnClickListener
import com.miguelzaragoza.upm.dam.viewmodel.CamerasViewModelFactory

class CamerasFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCamerasBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = CamerasViewModelFactory(application)
        val camerasViewModel = ViewModelProvider(this, viewModelFactory).get(CamerasViewModel::class.java)

        binding.lifecycleOwner = this
        binding.viewModel = camerasViewModel

        binding.camerasList.adapter = CamerasAdapter(OnClickListener {
            camerasViewModel.displayImageCamera(it)
        })

        return binding.root
    }

}