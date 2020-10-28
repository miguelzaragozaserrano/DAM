package com.miguelzaragoza.upm.dam.ui.cameras

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.miguelzaragoza.upm.dam.databinding.FragmentImageBinding
import com.miguelzaragoza.upm.dam.viewmodel.CamerasViewModelFactory

class ImageFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val binding = FragmentImageBinding.inflate(inflater)

        val application = requireNotNull(this.activity).application
        val viewModelFactory = CamerasViewModelFactory(application)
        val camerasViewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(CamerasViewModel::class.java)

        binding.lifecycleOwner = requireActivity()
        binding.viewModel = camerasViewModel

        return binding.root
    }

}