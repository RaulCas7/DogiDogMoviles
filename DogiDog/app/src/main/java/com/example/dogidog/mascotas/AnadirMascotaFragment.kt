package com.example.dogidog.mascotas

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dogidog.R
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.dataModels.MascotaCrear
import com.example.dogidog.dataModels.Raza
import com.example.dogidog.dataModels.Usuario
import com.example.dogidog.databinding.FragmentAnadirMascotaBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
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

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private lateinit var binding: FragmentAnadirMascotaBinding
    private var esMacho = true
    private var listaRazas: List<Raza> = emptyList()
    private var uriFotoSeleccionada: Uri? = null
    private val seleccionarFotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uriFotoSeleccionada = uri
            binding.imgMascota.setImageURI(uri)
            Log.d("ImageUri", "Imagen seleccionada: $uri")
        } else {
            Log.d("ImageUri", "No se seleccionó ninguna imagen.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAnadirMascotaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onPause() {
        super.onPause()

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

    private fun configurarSpinnerRazas() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val call = service.obtenerTodasLasRazas()
        call.enqueue(object : Callback<List<Raza>> {
            override fun onResponse(call: Call<List<Raza>>, response: Response<List<Raza>>) {
                if (response.isSuccessful) {
                    listaRazas = response.body() ?: emptyList()
                    val adapter = ArrayAdapter(
                        requireContext(),
                        R.layout.spinner_item,
                        listaRazas.map { it.nombre }
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
            val razaSeleccionada = obtenerRazaSeleccionada()
            val peso = binding.edtPeso.text.toString().trim()

            if (nombre.isEmpty() || fechaNacimiento.isEmpty() || peso.isEmpty() || razaSeleccionada == null) {
                mostrarToast(requireContext(), "Por favor, completa todos los campos")
                return@setOnClickListener
            }

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(fechaNacimiento)
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = outputFormat.format(date)

            val mascota = MascotaCrear(
                usuario = obtenerUsuarioLocal()!!,
                nombre = nombre,
                raza = razaSeleccionada,
                edad = calcularEdad(formattedDate),
                fechaNacimiento = formattedDate,
                peso = peso.toDouble(),
                genero = if (esMacho) "Macho" else "Hembra",
                esterilizado = false,
                fechaProximaVacunacion = "9999-01-01",
                fechaProximaDesparasitacion = "9999-01-01",
                foto = "",
                metrosRecorridos = 0L
            )

            guardarMascota(mascota)
        }
    }

    private fun calcularEdad(fechaNacimiento: String): Int {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val nacimientoDate = sdf.parse(fechaNacimiento)

        val calendar = Calendar.getInstance()
        val today = calendar.time

        val diffInMillis = today.time - nacimientoDate.time
        val diffInYears = diffInMillis / (1000L * 60 * 60 * 24 * 365)

        return diffInYears.toInt()
    }

    private fun guardarMascota(mascota: MascotaCrear) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        val call = service.guardarMascota(mascota)
        call.enqueue(object : Callback<Mascota> {
            override fun onResponse(call: Call<Mascota>, response: Response<Mascota>) {
                if (response.isSuccessful && response.body() != null) {
                    val mascotaCreada = response.body()!!

                    if (uriFotoSeleccionada != null) {
                        subirFotoMascota(mascotaCreada.id, mascotaCreada.nombre)
                    }

                    Toast.makeText(requireContext(), "Mascota ${mascotaCreada.nombre} añadida correctamente", Toast.LENGTH_SHORT).show()
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    mostrarToast(requireContext(), "Error al guardar la mascota")
                }
            }

            override fun onFailure(call: Call<Mascota>, t: Throwable) {
                mostrarToast(requireContext(), "Error en la conexión: ${t.message}")
            }
        })
    }

    fun mostrarToast(context: Context, mensaje: String) {
        val layout = LayoutInflater.from(context).inflate(R.layout.toast_personalizado, null)

        val textView = layout.findViewById<TextView>(R.id.toastText)
        textView.text = mensaje

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.show()
    }

    private fun obtenerUsuarioLocal(): Usuario? {
        val prefs = requireActivity().getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val id = prefs.getInt("usuario_id", -1)
        val usuario = prefs.getString("usuario", null)
        val email = prefs.getString("usuario_email", null)
        val password = prefs.getString("usuario_password", null)
        val contadorPreguntas = prefs.getInt("usuario_preguntas", 0)
        val latitud = prefs.getFloat("usuario_latitud", Float.MIN_VALUE)
        val longitud = prefs.getFloat("usuario_longitud", Float.MIN_VALUE)
        val valoracion = prefs.getInt("usuario_valoracion", 0)

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
                valoracion = valoracion
            )
        } else {
            null
        }
    }

    private fun configurarToolbar() {
        (activity as AppCompatActivity).supportActionBar?.apply {
            val titleTextView = TextView(requireContext()).apply {
                text = "Añadir mascota"
                setTextColor(Color.WHITE)
                setTypeface(null, Typeface.BOLD)

                val actionBarHeight = resources.getDimensionPixelSize(androidx.appcompat.R.dimen.abc_action_bar_default_height_material)

                val textSize = (actionBarHeight * 0.5f).toFloat()
                this.textSize = textSize / resources.displayMetrics.density
            }

            displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
            customView = titleTextView

            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(requireContext(), R.color.primario)))
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.baseline_arrow_back_24)

            setHasOptionsMenu(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().supportFragmentManager.popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun configurarBotonCambioFoto() {
        binding.btnCambiarFoto.setOnClickListener {
            seleccionarFotoLauncher.launch("image/*")
        }
    }

    private fun subirFotoMascota(idMascota: Int, nombreMascota: String) {
        if (uriFotoSeleccionada == null) {
            // Si no se ha seleccionado una foto, notificar al usuario
            Toast.makeText(context, "Por favor selecciona una foto para la mascota", Toast.LENGTH_SHORT).show()
            return
        }

        val contentResolver = requireContext().contentResolver
        val inputStream = contentResolver.openInputStream(uriFotoSeleccionada!!)

        // Asegúrate de que el inputStream no sea nulo
        if (inputStream == null) {
            Log.e("SubirFoto", "No se pudo obtener el stream de la imagen")
            return
        }

        val bytes = inputStream.readBytes()
        if (bytes.isEmpty()) {
            Log.e("SubirFoto", "El contenido de la imagen está vacío")
            return
        }

        val requestBody = RequestBody.create("image/*".toMediaTypeOrNull(), bytes)

        // Limpiar el nombre de la mascota y agregar la extensión ".jpg"
        val nombreArchivo = "${nombreMascota.replace(" ", "_").toLowerCase()}.jpg"

        // Crear la parte Multipart
        val fotoPart = MultipartBody.Part.createFormData("fichero", nombreArchivo, requestBody)

        // Llamada Retrofit
        val service = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        // Realizar la llamada al servidor
        val call = service.subirFotoMascota(idMascota, fotoPart)
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.d("SubirFoto", "Foto subida con éxito: ${response.body()}")
                    Toast.makeText(context, "Foto subida exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("SubirFoto", "Error al subir la foto: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Error al subir la foto", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("SubirFoto", "Error en la conexión: ${t.message}")
            }
        })
    }
}