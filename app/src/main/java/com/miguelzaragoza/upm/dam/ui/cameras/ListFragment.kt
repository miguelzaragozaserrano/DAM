package com.miguelzaragoza.upm.dam.ui.cameras

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.miguelzaragoza.upm.dam.databinding.FragmentListBinding
import com.miguelzaragoza.upm.dam.ui.common.CamerasAdapter
import com.miguelzaragoza.upm.dam.ui.common.OnClickListener
import com.miguelzaragoza.upm.dam.viewmodel.CamerasViewModelFactory

class ListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentListBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = CamerasViewModelFactory(application)
        val camerasViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(CamerasViewModel::class.java)

        binding.lifecycleOwner = requireActivity()
        binding.viewModel = camerasViewModel

        binding.camerasList.adapter = camerasViewModel.adapter

        return binding.root
    }

}