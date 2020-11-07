package com.miguelzaragoza.upm.dam.binding

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
import com.google.android.gms.maps.model.MarkerOptions
import com.miguelzaragoza.upm.dam.R
import com.miguelzaragoza.upm.dam.model.Camera
import com.miguelzaragoza.upm.dam.model.Cameras
import com.miguelzaragoza.upm.dam.modules.common.CamerasAdapter

/***************************** CLASE BINDING ADAPTERS *****************************
 Permite personalizar la lógica con la que un método set ejecuta un atributo del XML
 **********************************************************************************/

/**
 * Función que asigna la imagen de la cámara que se clickea al ImageView.
 * Para ello, le pasamos el View correspondiente a la función y el posible enlace de la imagen.
 * 1.- Comprobamos si el String no está vacío con el metodo "let".
 * 2.- Convertimos el String en un Uri.
 * 3.- Usamos la librería Glide para obtener la imagen de internet y cargarla en el ImageView.
 *     Durante la carga de la imagen añadimos una animación de carga y, en caso de que no se consiga
 *     cargar, mostramos una imagen como si el fichero estuviera roto.
 * @param imgView: vista del ImageView
 * @param imgUrl: enlace con la foto de la cámara que queremos mostrar
 * */
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?){
    imgUrl?.let{
        val imgUri = imgUrl.toUri().buildUpon().scheme("http").build()
        Glide.with(imgView.context)
                .load(imgUri)
                .apply(RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_broken_image))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imgView)
    }
}

/**
 * Función que asigna la lista de cámaras al RecyclerView.
 * Para ello, le pasamos el View correspondiente a la función y la posible lista de objetos Cámara.
 * 1.- Asignamos el adaptador CamerasAdapter a una variable.
 * 2.- Le pasamos la lista que queremos que muestre.
 * @param recyclerView: vista del RecyclerView
 * @param cameras: lista de cámaras
 */
@BindingAdapter("listCameras")
fun bindRecyclerView(recyclerView: RecyclerView, cameras: List<Camera>?){
    val adapter = recyclerView.adapter as CamerasAdapter
    adapter.setData(cameras)
}

@BindingAdapter("initMap")
fun bindMapView(mapView: MapView, cameras: Cameras?){
    mapView.onCreate(Bundle())
    mapView.getMapAsync {
        mapView.onResume()
        it.mapType = GoogleMap.MAP_TYPE_NORMAL

        val cameraPosition = CameraPosition.Builder().target(cameras?.get(0)?.coordinates).zoom(12F).build()
        it.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        cameras?.map { camera ->
            it.addMarker(
                    MarkerOptions()
                            .position(camera.coordinates)
                            .title(camera.name)
            )
        }
    }
}