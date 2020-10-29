package com.miguelzaragoza.upm.dam.ui.cameras

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.miguelzaragoza.upm.dam.R

/**
 * Activity que muestra el fragmento con la lista de imágenes y el fragmento con la imagen de
 * la cámara.
 */
class CamerasActivity : AppCompatActivity() {
    /* Función que se invoca cada vez que se crea el Activity */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /* Asignamos el layout correspondiente al Activity */
        setContentView(R.layout.activity_cameras)
    }
}