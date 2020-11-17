package com.miguelzaragoza.upm.dam.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miguelzaragoza.upm.dam.database.CameraDao
import com.miguelzaragoza.upm.dam.modules.ui.loading.LoadingViewModel

/**
 * ViewModelFactory que permite crear un LoadingViewModel al que pasarle parámetros por constructor
 */
class LoadingViewModelFactory (
        private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoadingViewModel::class.java)) {
            return LoadingViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}