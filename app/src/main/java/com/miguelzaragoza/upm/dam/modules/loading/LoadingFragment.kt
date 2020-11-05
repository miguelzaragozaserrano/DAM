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

    /******************************** VARIABLES BÁSICAS ********************************
     ***********************************************************************************/

    /**
     * Inicializamos con lazy nuestro [LoadingViewModel] para crearlo con un método
     * lifecycle apropiado.
     */
    private val loadingViewModel: LoadingViewModel by lazy{
        val application = requireNotNull(this.activity).application
        ViewModelProvider(this, LoadingViewModelFactory(application))
                .get(LoadingViewModel::class.java)
    }

    /******************************* FUNCIONES OVERRIDE *******************************
     **********************************************************************************/

    /**
     * Función que se llama inmediatamente después del return de onCreateView() y el
     * se haya creado la jerarquía de vistas del Fragment. Se puede utilizar para recuperar vistas
     * o restaurar estados.
     *
     * @param view Vista creada recientemente.
     * @param savedInstanceState Si no es nulo, este Fragment se está reconstruyendo a partir
     * de un estado guardado anterior como se indica aquí.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /* Observamos la variable navigateToCamerasFragment. Si toma un valor distinto de null,
        *  es debido a que se ha completado el ProgressBar Horizontal
        *  y por tanto navegamos a un segundo fragment */
        loadingViewModel.navigateToCamerasFragment.observe(viewLifecycleOwner, {
            if(it != null) findNavController()
                    .navigate(LoadingFragmentDirections
                            .actionSplashFragmentToCamerasFragment(loadingViewModel.list))
        })
    }

    /**
     * Función que se llama para instanciar la vista de interfaz de usuario (UI).
     *
     * @param inflater El objeto LayoutInflater se usa para inflar cualquier vista
     * en el Fragment.
     * @param container Si no es nulo, esta es la vista principal a la que se debe adjuntar
     * la UI del Fragment. El Fragment no debe agregar la vista en sí, pero esto puede
     * usarse para generar los LayoutParams de la vista.
     * @param savedInstanceState Si no es nulo, este Fragment se está reconstruyendo a partir
     * de un estado guardado anterior como se indica aquí.
     *
     * @return Devuelve la vista UI del Fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentLoadingBinding.inflate(inflater)

        /* Permite a Data Binding observar LiveData con el lifecycle de su Fragment */
        binding.lifecycleOwner = this

        /* Le asignamos al ProgressBar Circle la animación que nos interesa */
        loadingViewModel.animator
                .addUpdateListener { animation ->
                    binding.progressCircle.progress = animation.animatedValue as Int }

        /* Observamos la variable increaseProgressBar. Si toma un valor distinto de null,
        *  es debido a que se ha detectado un tick del objeto CountDownTimer y por tanto
        *  queremos aumentar el ProgressBar Horizontal */
        loadingViewModel.increaseProgressBar.observe(viewLifecycleOwner, {
            if(it != null) binding.millisUntilFinished = loadingViewModel.millisUntilFinished
        })

        return binding.root
    }

}