package com.miguelzaragoza.upm.dam.model

/**
 * Esta clase nos permite obtener la respuesta de la API de GoogleMaps para almacenar
 * los valores que nos interesan.
 */

class Response {
    var routes: ArrayList<Routes>? = null
}

class Routes{
    var legs: ArrayList<Legs>? = null
}

class Legs{
    var steps: ArrayList<Steps>? = null
}

class Steps{
    var polyline: Polyline? = null
}

class Polyline{
    var points: String = ""
}