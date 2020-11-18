package com.miguelzaragoza.upm.dam.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

/**
 * Clase Parcelable del objeto Camera.
 *
 * @param id Identificador de la cámara.
 * @param name Nombre de la cámara.
 * @param url Enlace con el que se obtiene la captura de dicha cámara.
 * @param latitude Latitud (coordenada) donde se localiza la cámara.
 * @param longitude Longitud (coordenada) donde se localiza la cámara.
 * @param selected Determina si tenemos la cámara seleccionada o no.
 * @param fav Determina si la cámara pertenece a las favoritas o no.
 */
@Entity(tableName = "camera_table")
@Parcelize
data class Camera(
    @PrimaryKey
    val id: Int,
    val name: String,
    val url: String,
    val latitude: Double,
    val longitude: Double,
    var selected: Boolean,
    var fav: Boolean
): Parcelable

/**
 * Clase Parcelable de la lista de objetos Camera.
 */
class Cameras: ArrayList<Camera>()