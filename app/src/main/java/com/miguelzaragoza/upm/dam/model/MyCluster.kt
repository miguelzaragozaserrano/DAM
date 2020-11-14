package com.miguelzaragoza.upm.dam.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * Clase del objeto MyCluster.
 *
 * @param position Posición que va a ocupar el marcador (cámara) en el mapa.
 * @param title Título que le asignamos al marcador (cámara) en el mapa.
 * @param snippet Snippet que le asignamos al marcador (cámara) en el mapa.
 */
class MyCluster(
    private val position: LatLng,
    private val title: String,
    private val snippet: String
) : ClusterItem {

    /**
     * Función que devuelve la posición.
     *
     * @return Objeto LatLng con la posición del marcador.
     */
    override fun getPosition(): LatLng {
        return position
    }

    /**
     * Función que devuelve el título.
     *
     * @return Título del marcador.
     */
    override fun getTitle(): String? {
        return title
    }

    /**
     * Función que devuelve el snippet.
     *
     * @return Snippet del marcador.
     */
    override fun getSnippet(): String? {
        return snippet
    }

}
