package com.miguelzaragoza.upm.dam

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

/**
 * Activity principal. Actua de Host para la navegación entre fragmentos.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}