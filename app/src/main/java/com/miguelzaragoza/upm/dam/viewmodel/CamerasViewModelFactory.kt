package com.miguelzaragoza.upm.dam.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miguelzaragoza.upm.dam.ui.cameras.CamerasViewModel

/**
 * ViewModelFactory que permite crear un ViewModel al que pasarle parámetros por constructor
 */
class CamerasViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CamerasViewModel::class.java)) {
            return CamerasViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}