package com.miguelzaragoza.upm.dam.binding

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.miguelzaragoza.upm.dam.R
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.model.Cameras
import com.miguelzaragoza.upm.dam.model.MyCluster
import com.miguelzaragoza.upm.dam.modules.ui.common.CamerasAdapter

/**
 * Función que asigna la imagen de la cámara que se clickea al ImageView.
 *
 * @param imgView Vista del ImageView.
 * @param imgUrl Enlace a la cámara que nos devuelve la foto que queremos mostrar.
 */
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?){
    imgUrl?.let{
        val imgUri = imgUrl.toUri().buildUpon().scheme("http").build()
        Glide.with(imgView.context)
                .load(imgUri)
                .apply(RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.broken_image))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imgView)
    }
}

/**
 * Función que asigna la lista de cámaras al RecyclerView.
 *
 * @param recyclerView Vista del RecyclerView.
 * @param cameras Lista de cámaras.
 */
@BindingAdapter("listCameras")
fun bindRecyclerView(recyclerView: RecyclerView, cameras: List<Camera>?){
    val adapter = recyclerView.adapter as CamerasAdapter
    adapter.setData(cameras)
}

/**
 * Función que inicia el Mapa.
 *
 * @param mapView Vista del MapView.
 * @param cameras Lista de cámaras.
 * @param cluster Variable que determina si queremos el modo Cluster o no.
 */
@BindingAdapter(value = ["bind:cameras", "bind:cluster"])
fun bindMapView(mapView: MapView, cameras: Cameras?, cluster: Boolean){
    /* Creamos el mapa */
    mapView.onCreate(Bundle())

    mapView.getMapAsync {
        mapView.onResume()
        /* Le asignamos por defecto el tipo normal */
        it.mapType = GoogleMap.MAP_TYPE_NORMAL
        /* Recorremos la lista de cámaras para hacer zoom en la seleccionada */
        for(camera in cameras!!){
            if(camera.selected){
                val cameraPosition =
                        CameraPosition
                                .Builder()
                                .target(LatLng(camera.latitude, camera.longitude))
                                .zoom(12F).build()
                it.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }

        /* Analizamos si queremos Cluster o no */
        if(cluster)
            setUpCluster(it, mapView.context, cameras)
        else{
            /* En caso de que no, añadimos las cámaras al mapa */
            cameras.map { camera ->
                it.addMarker(
                        MarkerOptions()
                                .position(LatLng(camera.latitude, camera.longitude))
                                .title(camera.name)
                )
            }
        }
    }
}

/**
 * Función que configura el Cluster.
 *
 * @param map Mapa al que añadiremos el Cluster.
 * @param context Contexto.
 * @param cameras Lista de cámaras.
 */
fun setUpCluster(map: GoogleMap, context: Context, cameras: Cameras){

    /* Iniciamos el gestor con el contexto y el mapa  */
    val clusterManager: ClusterManager<MyCluster> = ClusterManager(context, map)

    /* Declaramos los listeners implementados por el Cluster */
    map.setOnCameraIdleListener(clusterManager)
    map.setOnMarkerClickListener(clusterManager)

    /* Añadimos las cámaras al gestor */
    addItems(clusterManager, cameras)
}

/**
 * Función que recorre la lista de cámaras para ir añadiéndolos al gestor.
 */
fun addItems(clusterManager: ClusterManager<MyCluster>, cameras: Cameras) {
    for (camera in cameras) {
        /* Creamos un objeto MyCluster para añadirlo al gestor */
        val item =
            MyCluster(LatLng(camera.latitude, camera.longitude),
                    "Title ${camera.name}",
                    "Snippet ${camera.id}"
            )
        clusterManager.addItem(item)
    }
}