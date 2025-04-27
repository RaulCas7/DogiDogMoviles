package com.example.dogidog.mascotas

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.dataModels.Raza
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.FragmentAnadirMascotaBinding
import com.example.dogidog.principal.PantallaPrincipalActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private var listaRazas: List<Raza> = emptyList()  // Lista de razas disponibles

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnadirMascotaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onPause() {
        super.onPause()

        // Mostrar la barra de navegación inferior (BottomNavigationView)
        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.navegacion)
        bottomNav.visibility = View.VISIBLE
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.navegacion)
        bottomNav.visibility = View.GONE

        configurarToolbar()
        configurarSpinnerRazas()
        configurarFechaNacimiento()
        configurarBotonCambioFoto()
        configurarBotonCambioSexo()
        configurarBotonGuardar()
    }

    // Método para configurar el Spinner con las razas
    private fun configurarSpinnerRazas() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Cambia esto si estás usando un emulador
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        // Llamada a la API para obtener las razas
        val call = service.obtenerTodasLasRazas()

        call.enqueue(object : Callback<List<Raza>> {
            override fun onResponse(call: Call<List<Raza>>, response: Response<List<Raza>>) {
                if (response.isSuccessful) {
                    listaRazas = response.body() ?: emptyList()
                    // Llenar el Spinner con las razas obtenidas
                    val adapter = ArrayAdapter(
                        requireContext(),
                        R.layout.spinner_item,
                        listaRazas.map { it.nombre } // Solo los nombres de las razas
                    )
                    adapter.setDropDownViewResource(R.layout.spinner_item)
                    binding.spinnerRaza.adapter = adapter
                } else {
                    mostrarToast(requireContext(), "Error al obtener las razas: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Raza>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // Obtener la raza seleccionada del spinner
    private fun obtenerRazaSeleccionada(): Raza? {
        val razaSeleccionadaNombre = binding.spinnerRaza.selectedItem.toString()
        return listaRazas.find { it.nombre == razaSeleccionadaNombre }
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
            val razaSeleccionada = obtenerRazaSeleccionada()  // Obtener el objeto Raza completo
            val peso = binding.edtPeso.text.toString().trim()

            if (nombre.isEmpty() || fechaNacimiento.isEmpty() || peso.isEmpty() || razaSeleccionada == null) {
                mostrarToast(requireContext(), "Por favor, completa todos los campos")
                return@setOnClickListener
            }

            // Convertir la fecha al formato correcto (yyyy-MM-dd)
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(fechaNacimiento)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = outputFormat.format(date)

            // Crear el objeto Mascota con los datos
            val mascota = Mascota(
                id = 0, // Asignar el ID correspondiente cuando se guarde en la base de datos
                usuario = obtenerUsuarioLocal()!!, // Asignar el usuario correspondiente
                nombre = nombre,
                raza = razaSeleccionada, // Usar el objeto Raza completo
                edad = calcularEdad(formattedDate), // Llamar a la función para calcular la edad usando la nueva fecha formateada
                fechaNacimiento = formattedDate,  // Usamos la fecha formateada
                peso = peso.toDouble(),
                genero = if (esMacho) "Macho" else "Hembra", // Usamos la variable `esMacho` para definir el género
                esterilizado = false, // Asignar según sea necesario
                fechaProximaVacunacion = "9999-01-01", // Asignar según sea necesario
                fechaProximaDesparasitacion = "9999-01-01", // Asignar según sea necesario
                foto = "", // Asignar la URL de la foto si es necesario
                metrosRecorridos = 0L // Asignar según sea necesario
            )

            // Guardar la mascota
            guardarMascota(mascota)
        }
    }

    private fun calcularEdad(fechaNacimiento: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())  // Usar el formato "yyyy-MM-dd"
        val nacimientoDate = sdf.parse(fechaNacimiento)

        val calendar = Calendar.getInstance()
        val today = calendar.time

        val diffInMillis = today.time - nacimientoDate.time
        val diffInYears = diffInMillis / (1000L * 60 * 60 * 24 * 365)

        return diffInYears.toInt()
    }

    private fun guardarMascota(mascota: Mascota) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Dirección de tu servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        // Realizamos la llamada a la API para guardar la mascota
        val call = service.guardarMascota(mascota)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Mascota ${mascota.nombre} añadida correctamente", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack() // Volver al fragment anterior
                } else {
                    mostrarToast(requireContext(), "Error al añadir la mascota")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Función para mostrar un mensaje personalizado de Toast
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

    // Función para obtener el usuario local desde SharedPreferences
    private fun obtenerUsuarioLocal(): Usuario? {
        val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val id = prefs.getInt("usuario_id", -1)
        val usuario = prefs.getString("usuario", null)
        val email = prefs.getString("usuario_email", null)
        val password = prefs.getString("usuario_password", null)
        val contadorPreguntas = prefs.getInt("usuario_preguntas", 0)
        val latitud = prefs.getFloat("usuario_latitud", Float.MIN_VALUE)
        val longitud = prefs.getFloat("usuario_longitud", Float.MIN_VALUE)
        val valoracion = prefs.getInt("usuario_valoracion", 0)  // Nuevo campo de valoración

        return if (id != -1 && usuario != null && email != null && password != null) {
            val latitudDouble = if (latitud != Float.MIN_VALUE) latitud.toDouble() else null
            val longitudDouble = if (longitud != Float.MIN_VALUE) longitud.toDouble() else null

            Usuario(
                id = id,
                usuario = usuario,
                email = email,
                password = password,
                contadorPreguntas = contadorPreguntas,
                latitud = latitudDouble,
                longitud = longitudDouble,
                valoracion = valoracion  // Agregar la valoración al objeto Usuario
            )
        } else {
            null
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().supportFragmentManager.popBackStack() // Volver al fragment anterior
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}