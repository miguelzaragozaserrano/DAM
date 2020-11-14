package com.miguelzaragoza.upm.dam.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.miguelzaragoza.upm.dam.model.Camera

/**
 * Base de datos de cámaras.
 */
@Database(entities = [Camera::class],
    version = 1,
    exportSchema = false)
abstract class CameraDatabase: RoomDatabase(){
    abstract val cameraDao: CameraDao

    /* Instanciamos la base de datos para no
    *  tener que hacerlo cada vez que llamamos a una función */
    companion object{
        @Volatile
        private var INSTANCE: CameraDatabase? = null

        /**
         * Función que nos devuelve la instancia de la base de datos.
         *
         * @param context Contexto.
         *
         * @return Base de datos.
         */
        fun getInstance(context: Context): CameraDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CameraDatabase::class.java,
                        "favorite_cameras_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}