package com.miguelzaragoza.upm.dam.modules.ui.loading

import android.animation.ValueAnimator
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.miguelzaragoza.upm.dam.databinding.FragmentLoadingBinding
import com.miguelzaragoza.upm.dam.modules.ui.common.DoInBackground

/**
* Fragment que muestra la primera pantalla
* donde aparece un mensaje de "cargando".
*/
class LoadingFragment : Fragment() {

    /******************************* FUNCIONES OVERRIDE *******************************
     **********************************************************************************/

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
        /* Bloqueamos rotar la pantalla */
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val binding = FragmentLoadingBinding.inflate(inflater)

        /* Permite a Data Binding observar LiveData con el lifecycle de su Fragment */
        binding.lifecycleOwner = this

        /* Le asignamos al ProgressBar Circle la animación que nos interesa */
        val animator: ValueAnimator = ValueAnimator.ofInt(0, 100)
        animator.addUpdateListener { animation ->
                    binding.progressCircle.progress = animation.animatedValue as Int }

        /* Creamos la tarea en segundo plano */
        DoInBackground(
                binding.progressHorizontal,
                requireNotNull(this.activity).application,
                binding.textLoading
        )

        return binding.root
    }

    override fun onPause() {
        super.onPause()
        DoInBackground.navigateToCamerasFragment.removeObservers(viewLifecycleOwner)
    }

    override fun onResume() {
        super.onResume()
        DoInBackground.navigateToCamerasFragment.observe(viewLifecycleOwner, {
            if(it != null) findNavController()
                .navigate(LoadingFragmentDirections
                    .actionSplashFragmentToCamerasFragment(DoInBackground.list)
                )
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        DoInBackground.showListComplete()
    }

}