package com.example.dogidog.mascotas

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dogidog.R
import com.example.dogidog.adapters.MascotaPagerAdapter
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.databinding.FragmentMascotaPrincipalBinding
import com.example.dogidog.principal.PantallaPrincipalActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MascotaPrincipalFragment : Fragment() {

    lateinit var binding: FragmentMascotaPrincipalBinding
    lateinit var mascota : Mascota

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMascotaPrincipalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mascota = arguments?.getParcelable("mascota")!!
        if (mascota != null) {
            configurarToolbar(mascota)
        }

        mascota?.let {
            binding.txtNombre.text = it.nombre
            binding.txtRaza.text = it.raza.nombre
            binding.imgFoto.setImageResource(
                if (it.genero == "Macho") R.drawable.bordercollie
                else R.drawable.borderhembra
                )

            binding.imgSexo.setImageResource(
                if (it.genero == "Macho") R.drawable.baseline_male_24
                else R.drawable.baseline_female_24
            )


        }

        if (savedInstanceState == null) {

            val bundle = Bundle()
            bundle.putParcelable("mascota", mascota) // Pasa la mascota al siguiente fragmento

            val mascotaInfoFragment = MascotaInfoFragment()
            mascotaInfoFragment.arguments = bundle // Establece los argumentos en el fragmento

            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container, mascotaInfoFragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }

        gestionarTabla()


    }

    override fun onResume() {
        super.onResume()
        (activity as? PantallaPrincipalActivity)?.binding?.navegacion?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        (activity as? PantallaPrincipalActivity)?.binding?.navegacion?.visibility = View.VISIBLE
    }

    private fun configurarToolbar(mascota: Mascota) {
        // Acceder al ActionBar de la Activity que contiene este Fragment
        (activity as AppCompatActivity).supportActionBar?.apply {
            // Crear el TextView personalizado
            val titleTextView = TextView(requireContext()).apply {

                text = mascota.nombre
                setTextColor(Color.WHITE) // Establecer el color blanco
                setTypeface(null, Typeface.BOLD) // Poner el texto en negrita

                // Obtener la altura de la ActionBar (Toolbar)
                val actionBarHeight = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material)

                // Ajustar el tamaño del texto proporcionalmente
                val textSize = (actionBarHeight * 0.5f).toFloat() // El tamaño del texto será el 50% de la altura
                this.textSize = textSize / resources.displayMetrics.density // Convertir a SP
            }

            // Establecer el título con el TextView personalizado
            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            customView = titleTextView

            // Cambiar el fondo de la ActionBar (Toolbar)
            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.primario)))

            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24) // Usa tu propio ícono de flecha

            setHasOptionsMenu(true)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().supportFragmentManager.popBackStack()
                requireActivity().supportFragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun gestionarTabla(){
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> replaceFragment(MascotaInfoFragment(), mascota)
                    1 -> replaceFragment(GraficaPesoFragment(), mascota)
                    2 -> replaceFragment(DocumentacionFragment(), mascota)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        Log.d("MascotaPrincipal", "Pestaña Info deseleccionada")
                    }
                    1 -> {
                        Log.d("MascotaPrincipal", "Pestaña Peso deseleccionada")
                    }
                    2 -> {
                        Log.d("MascotaPrincipal", "Pestaña Documentación deseleccionada")
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.d("MascotaPrincipal", "Pestaña seleccionada nuevamente: ${tab?.text}")
            }
        })
    }

    private fun replaceFragment(fragment: Fragment, mascota: Mascota) {
        val bundle = Bundle()
        bundle.putParcelable("mascota", mascota)  // Pasar los datos de la mascota
        fragment.arguments = bundle

        parentFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

}
