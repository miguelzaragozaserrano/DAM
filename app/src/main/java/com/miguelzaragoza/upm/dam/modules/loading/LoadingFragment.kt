package com.miguelzaragoza.upm.dam.modules.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.miguelzaragoza.upm.dam.databinding.FragmentLoadingBinding
import com.miguelzaragoza.upm.dam.viewmodel.LoadingViewModelFactory

/**
* Fragment que muestra la primera pantalla
* donde aparece un mensaje de "cargando"
*/
class LoadingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /* Usamos la librería DataBinding para inflar el Fragment con el layout correspondiente */
        val binding = FragmentLoadingBinding.inflate(inflater)

        /* Obtenemos el activity de la aplicación para pasarselo como parámetro al ViewModel y
        *  de esa manera poder obtener el contexto correspondiente allí */
        val application = requireNotNull(this.activity).application

        /* Como necesitamos pasarle un parámetro al ViewModel por el constructor,
        *  tenemos que crear un ViewModelFactory */
        val viewModelFactory = LoadingViewModelFactory(application)

        /* Declaramos nuestra variable del ViewModel para poder interactuar con él */
        val loadingViewModel = ViewModelProvider(this, viewModelFactory).get(LoadingViewModel::class.java)

        /* Asignamos al lifecycleOwner el fragment actual para detectar
        *  los cambios de los objetos LiveData */
        binding.lifecycleOwner = this

        /* Le asignamos al ProgressBar Circle la animación que nos interesa */
        loadingViewModel.animator.addUpdateListener { animation -> binding.progressCircle.progress = animation.animatedValue as Int }

        /* Observamos la variable increaseProgressBar. Si toma un valor distinto de null,
        *  es debido a que se ha detectado un tick del objeto CountDownTimer y por tanto
        *  queremos aumentar el ProgressBar Horizontal */
        loadingViewModel.increaseProgressBar.observe(viewLifecycleOwner, {
            if(it != null) binding.millisUntilFinished = loadingViewModel.millisUntilFinished.value
        })

        /* Observamos la variable navigateToCamerasFragment. Si toma un valor distinto de null,
        *  es debido a que se ha completado el ProgressBar Horizontal
        *  y por tanto navegamos a un segundo fragment */
        loadingViewModel.navigateToCamerasFragment.observe(viewLifecycleOwner, {
            if(it != null) findNavController().navigate(LoadingFragmentDirections.actionSplashFragmentToCamerasFragment(loadingViewModel.list))
        })

        /* Devolvemos la vista más externa del layout asociado con el binding */
        return binding.root
    }

}