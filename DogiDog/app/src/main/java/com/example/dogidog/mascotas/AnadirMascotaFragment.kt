package com.example.dogidog.mascotas

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.example.dogidog.R
import com.example.dogidog.databinding.FragmentAnadirMascotaBinding
import com.example.dogidog.principal.PantallaPrincipalActivity
import java.util.Calendar

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AnadirMascotaFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AnadirMascotaFragment : Fragment() {

        private lateinit var binding: FragmentAnadirMascotaBinding
        private var esMacho = true

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            binding = FragmentAnadirMascotaBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            configurarToolbar()
            configurarSpinnerRazas()
            configurarFechaNacimiento()
            configurarBotonCambioFoto()
            configurarBotonCambioSexo()
            configurarBotonGuardar()
        }

        private fun configurarSpinnerRazas() {
            val adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.raza_array, // Usa tu propio array de razas en strings.xml
                R.layout.spinner_item
            )
            adapter.setDropDownViewResource(R.layout.spinner_item)
            binding.spinnerRaza.adapter = adapter
        }

        private fun configurarFechaNacimiento() {
            binding.btnCalendario.setOnClickListener {
                val calendario = Calendar.getInstance()
                val anio = calendario.get(Calendar.YEAR)
                val mes = calendario.get(Calendar.MONTH)
                val dia = calendario.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                    val fechaSeleccionada = "$dayOfMonth/${month + 1}/$year"
                    binding.etFecha.setText(fechaSeleccionada)
                }, anio, mes, dia)

                datePickerDialog.show()
            }
        }

        // Configurar botón para cambiar la imagen de la mascota
        private fun configurarBotonCambioFoto() {
            binding.btnCambiarFoto.setOnClickListener {
                Toast.makeText(requireContext(), "Función de cambio de foto no implementada", Toast.LENGTH_SHORT).show()
            }
        }

        private fun configurarBotonCambioSexo() {
            binding.btnCambiarSexo.setOnClickListener {
                esMacho = !esMacho
                val imagenSexo = if (esMacho) R.drawable.baseline_male_24 else R.drawable.baseline_female_24
                binding.imgSexo.setImageResource(imagenSexo)
            }
        }

    private fun configurarBotonGuardar() {
        binding.btnAnadirMascota.setOnClickListener {
            val nombre = binding.edtNombre.text.toString().trim()
            val fechaNacimiento = binding.etFecha.text.toString().trim()
            val raza = binding.spinnerRaza.selectedItem.toString()
            val peso = binding.edtPeso.text.toString().trim()

            if (nombre.isEmpty() || fechaNacimiento.isEmpty() || peso.isEmpty()) {
                mostrarToast(requireContext(), "Por favor, completa todos los campos")
                return@setOnClickListener
            }

            // Guardar la mascota en la base de datos o donde sea necesario
            Toast.makeText(requireContext(), "Mascota $nombre añadida correctamente", Toast.LENGTH_SHORT).show()
        }
        }

    override fun onResume() {
        super.onResume()
        (activity as? PantallaPrincipalActivity)?.binding?.navegacion?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        (activity as? PantallaPrincipalActivity)?.binding?.navegacion?.visibility = View.VISIBLE
    }
    private fun configurarToolbar() {
        // Acceder al ActionBar de la Activity que contiene este Fragment
        (activity as AppCompatActivity).supportActionBar?.apply {
            // Crear el TextView personalizado
            val titleTextView = TextView(requireContext()).apply {
                text = "Añadir mascota"
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

    // Manejar el botón de retroceso
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().supportFragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun mostrarToast(context: Context, mensaje: String) {
        val layout = LayoutInflater.from(context).inflate(R.layout.toast_personalizado, null)

        val textView = layout.findViewById<TextView>(R.id.toastText)
        textView.text = mensaje

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.setGravity(Gravity.CENTER, 0, 0) // Lo centra en la pantalla
        toast.show()
    }
}