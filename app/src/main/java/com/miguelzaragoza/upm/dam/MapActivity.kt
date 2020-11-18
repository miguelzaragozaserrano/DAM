package com.miguelzaragoza.upm.dam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.miguelzaragoza.upm.dam.model.Camera

class MapActivity : AppCompatActivity() {

    /******************************* FUNCIONES OVERRIDE *******************************
     **********************************************************************************/

    /**
     * Función que se llama para crear el activity.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        /* Añadimos un botón de retroceso */
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     *  Función que nos permite navegar hacia atrás si pulsamos el botón
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    /******************************** FUNCIONES BÁSICAS *******************************
     **********************************************************************************/

    /**
     * Función que nos devuelve la lista de cámaras que queremos mostrar en el mapa.
     */
    fun getSharedList(): ArrayList<Camera> {
        return intent.getParcelableArrayListExtra("sharedList")!!
    }

    /**
     * Función que nos devuelve el Boolean que determina si activamos o no el modo cluster.
     */
    fun getCluster(): Boolean{
        return intent.extras?.getBoolean("cluster")!!
    }
}