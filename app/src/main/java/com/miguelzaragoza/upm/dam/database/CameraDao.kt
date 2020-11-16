package com.miguelzaragoza.upm.dam.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.miguelzaragoza.upm.dam.model.Camera

/**
 * DAO que declara la interfaz común entre la aplicación y Room.
 */
@Dao
interface CameraDao {
    /**
     * Función suspendida que inserta una cámara en la base de datos.
     *
     * @param camera Cámara que queremos insertar.
     */
    @Insert
    suspend fun insert(camera: Camera)

    /**
     * Función suspendida que elimina una cámara en la base de datos.
     *
     * @param camera Cámara que queremos insertar.
     */
    @Delete
    suspend fun remove(camera: Camera)

    /**
     * Función suspendida que selecciona una cámara en la base de datos.
     *
     * @param id Identificador de la cámara.
     * @return Cámara solicitada
     */
    @Query("SELECT * from camera_table WHERE id = :id")
    suspend fun get(id: Int): Camera?

    /**
     * Función suspendida que actualiza el valor "selected" de la cámara.
     *
     * @param selected Boolean que determina si tenemos la cámara seleccionada o no.
     */
    @Query("UPDATE camera_table SET selected = :selected")
    suspend fun updateSelected(selected: Boolean)

    /**
     * Función suspendida que vacía la base de datos.
     */
    @Query("DELETE FROM camera_table")
    suspend fun clear()

    /**
     * Funcion suspendida que devuelve el número de filas de la tabla.
     *
     * @return Número de filas
     */
    @Query("SELECT COUNT (*) FROM camera_table")
    fun getSize(): LiveData<Int>

}