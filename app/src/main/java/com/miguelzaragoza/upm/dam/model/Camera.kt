package com.miguelzaragoza.upm.dam.model

/**
 * Clase del objeto Camera.
 * Consta de:
 * 1.- Un nombre.
 * 2.- Una url para acceder a la imagen que captura.
 * 3.- Unas coordenadas de su ubicación.
 * 4.- Un estado que nos indica si es la cámara que
 *     está mostrándose actualmente (true) o no (false).
 */
data class Camera(
    val name: String,
    val url: String,
    val coordinates: String,
    var status: Boolean
)