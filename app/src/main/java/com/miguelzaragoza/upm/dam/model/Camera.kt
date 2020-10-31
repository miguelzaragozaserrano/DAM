package com.miguelzaragoza.upm.dam.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Clase del objeto Camera.
 * Consta de:
 * 1.- Un nombre.
 * 2.- Una url para acceder a la imagen que captura.
 * 3.- Unas coordenadas de su ubicación.
 * 4.- Un estado que nos indica si es la cámara que
 *     está mostrándose actualmente (true) o no (false).
 * @param name: nombre de la cámara
 * @param url: enlace con el que se obtiene la captura de dicha cámara
 * @param coordinates: coordenadas de donde se ubica la cámara
 * @param status: valor que determina si tenemos la cámara seleccionada o no
 */
@Parcelize
data class Camera(
    val name: String,
    val url: String,
    val coordinates: String,
    var status: Boolean
): Parcelable

@Parcelize
class Cameras: ArrayList<Camera>(), Parcelable