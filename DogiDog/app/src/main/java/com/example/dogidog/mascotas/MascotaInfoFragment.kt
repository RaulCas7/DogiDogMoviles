package com.example.dogidog.mascotas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dogidog.R
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.databinding.FragmentMascotaInfoBinding


class MascotaInfoFragment : Fragment() {
    lateinit var binding: FragmentMascotaInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMascotaInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mascota: Mascota? = arguments?.getParcelable("mascota")



        mascota?.let {
            binding.txtNumEdad.text = it.edad.toString() + " años"
            binding.txtNumPeso.text = it.peso.toString() + " kg"
            binding.txtFechaVacuna.text = it.fechaProximaVacunacion
            binding.txtFechaDesparasitacion.text = it.fechaProximaDesparasitacion
        } ?: run {
            // Si mascota es null, puedes manejar el caso o mostrar un mensaje predeterminado
            binding.txtNumEdad.text = getString(R.string.sinInfo)
            binding.txtNumPeso.text = getString(R.string.sinInfo)
            binding.txtFechaVacuna.text = getString(R.string.sinInfo)
            binding.txtFechaDesparasitacion.text = getString(R.string.sinInfo)
            binding.txtMarcaPienso.text = getString(R.string.sinInfo)
        }
    }
}