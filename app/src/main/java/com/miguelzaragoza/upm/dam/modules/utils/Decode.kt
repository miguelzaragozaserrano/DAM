package com.miguelzaragoza.upm.dam.modules.utils

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

/**
 * Función que descodifica las coordenadas del Polyline.
 *
 * @param encoded String codificado con las coordenadas
 * @param coordinates ArrayList que almacena las coordenadas
 */
fun decodePoly(encoded: String, coordinates: PolylineOptions) {
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0
    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat
        shift = 0
        result = 0
        do {
            b = encoded[index++].toInt() - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng
        val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
        coordinates.add(p)
    }
}