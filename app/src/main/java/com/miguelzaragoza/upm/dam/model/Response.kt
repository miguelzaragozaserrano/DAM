package com.miguelzaragoza.upm.dam.model

/**
 * Clase que gestiona la respuesta de la API de Google Maps.
 */

class Response {
    var routes:ArrayList<Routes>? = null
}

class Routes{
    var legs:ArrayList<Legs>? = null
}

class Legs{
    var steps:ArrayList<Steps>? = null
}

class Steps{
    var polyline: Polyline? = null
}

class Polyline{
    var points:String = ""
}