package com.example.dogidog.mascotas

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.dogidog.R
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.databinding.FragmentMascotaInfoBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class MascotaInfoFragment : Fragment() {
    lateinit var binding: FragmentMascotaInfoBinding
    lateinit var mascota : Mascota

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMascotaInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mascota = arguments?.getParcelable("mascota")!!



        mascota?.let {
            binding.txtNumEdad.text = it.edad.toString() + " años"
            binding.txtNumPeso.text = it.peso.toString() + " kg"

            if(mascota.genero.equals("Hembra")){
                if(mascota.peso!! < mascota.raza.pesoMinHembra){
                    binding.txtNumPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                    binding.txtNumPeso.text = "${mascota.peso ?: "?"} kg"
                    binding.txtProblemaPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                    binding.txtProblemaPeso.text = "(Infrapeso)"
                    binding.txtProblemaPeso.isVisible = true
                }else if (mascota.peso!! > mascota.raza.pesoMaxHembra){
                    binding.txtNumPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                    binding.txtNumPeso.text = "${mascota.peso ?: "?"} kg "
                    binding.txtProblemaPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                    binding.txtProblemaPeso.text = "(Sobrepeso)"
                    binding.txtProblemaPeso.isVisible = true
                }
            }else if(mascota.genero.equals("Macho")){
                if(mascota.peso!! < mascota.raza.pesoMinMacho){
                    binding.txtNumPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                    binding.txtNumPeso.text = "${mascota.peso ?: "?"} kg"
                    binding.txtProblemaPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                    binding.txtProblemaPeso.text = "(Infrapeso)"
                    binding.txtProblemaPeso.isVisible = true
                }else if (mascota.peso!! > mascota.raza.pesoMaxMacho){
                    binding.txtNumPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                    binding.txtNumPeso.text = "${mascota.peso ?: "?"} kg"
                    binding.txtProblemaPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                    binding.txtProblemaPeso.text = "(Sobrepeso)"
                    binding.txtProblemaPeso.isVisible = true
                }
            }

            val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaActual = Date()

            val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // formato original
            val formatoSalida = SimpleDateFormat("dd-MM-yyyy", Locale("es", "ES")) // formato español

            binding.txtFechaVacuna.text = mascota.fechaProximaVacunacion?.let {
                try {
                    val fecha = formatoEntrada.parse(it)
                    if (fecha != null) formatoSalida.format(fecha) else getString(R.string.sinInfo)
                } catch (e: ParseException) {
                    getString(R.string.sinInfo)
                }
            } ?: getString(R.string.sinInfo)

            binding.txtFechaDesparasitacion.text = mascota.fechaProximaDesparasitacion?.let {
                try {
                    val fecha = formatoEntrada.parse(it)
                    if (fecha != null) formatoSalida.format(fecha) else getString(R.string.sinInfo)
                } catch (e: ParseException) {
                    getString(R.string.sinInfo)
                }
            } ?: getString(R.string.sinInfo)

// Verificamos y coloreamos la fecha de vacunación
            mascota.fechaProximaVacunacion?.let { fechaStr ->
                try {
                    val fechaVacuna = formatoFecha.parse(fechaStr)
                    if (fechaVacuna != null && fechaVacuna.before(fechaActual)) {
                        binding.txtFechaVacuna.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }

// Verificamos y coloreamos la fecha de desparasitación
            mascota.fechaProximaDesparasitacion?.let { fechaStr ->
                try {
                    val fechaDesparasitacion = formatoFecha.parse(fechaStr)
                    if (fechaDesparasitacion != null && fechaDesparasitacion.before(fechaActual)) {
                        binding.txtFechaDesparasitacion.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                    }
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
            binding.txtMarcaPienso.text = it.pienso
            binding.numeroMicrochip.text = it.microchip
        } ?: run {
            // Si mascota es null, puedes manejar el caso o mostrar un mensaje predeterminado
            binding.txtNumEdad.text = getString(R.string.sinInfo)
            binding.txtNumPeso.text = getString(R.string.sinInfo)
            binding.txtFechaVacuna.text = getString(R.string.sinInfo)
            binding.txtFechaDesparasitacion.text = getString(R.string.sinInfo)
            binding.txtMarcaPienso.text = getString(R.string.sinInfo)
        }

        binding.fabEditarMascota.setOnClickListener {
            mostrarDialogoEditarMascota(mascota)
        }

    }
    private fun mostrarDialogoEditarMascota(mascota: Mascota) {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_editar_mascota, null)

        val inputPeso = view.findViewById<EditText>(R.id.etPeso)
        val inputPienso = view.findViewById<EditText>(R.id.etPienso)
        val inputMicrochip = view.findViewById<EditText>(R.id.etMicrochip) // Agregamos el input de microchip
        val fechaVacuna = view.findViewById<TextView>(R.id.tvFechaVacuna)
        val fechaDesp = view.findViewById<TextView>(R.id.tvFechaDesp)

        // Mostrar los valores actuales de la mascota en los TextView a la derecha
        view.findViewById<TextView>(R.id.tvPesoActual).text = "${mascota?.peso} kg"
        view.findViewById<TextView>(R.id.tvFechaVacunaActual).text = "${mascota?.fechaProximaVacunacion}"
        view.findViewById<TextView>(R.id.tvFechaDespActual).text = "${mascota?.fechaProximaDesparasitacion}"
        view.findViewById<TextView>(R.id.tvPiensoActual).text = mascota?.pienso ?: "No especificado"
        view.findViewById<TextView>(R.id.tvMicrochipActual).text = mascota?.microchip ?: "No especificado"

        // Establecer el microchip actual
        inputMicrochip.setText(mascota?.microchip ?: "")

        inputPeso.setText(mascota?.peso?.toString() ?: "")
        inputPienso.setText(mascota?.pienso ?: "")
        fechaVacuna.text = mascota?.fechaProximaVacunacion?.toString() ?: "Seleccionar fecha"
        fechaDesp.text = mascota?.fechaProximaDesparasitacion?.toString() ?: "Seleccionar fecha"

        val calendar = Calendar.getInstance()

        val datePickerListener = { textView: TextView ->
            val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                val fecha = "%04d-%02d-%02d".format(year, month + 1, dayOfMonth)
                textView.text = fecha
            }
            DatePickerDialog(
                requireContext(),
                listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        fechaVacuna.setOnClickListener { datePickerListener(fechaVacuna) }
        fechaDesp.setOnClickListener { datePickerListener(fechaDesp) }

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Datos de la Mascota")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val pesoNuevo = inputPeso.text.toString()
                val piensoNuevo = inputPienso.text.toString()
                val microchipNuevo = inputMicrochip.text.toString() // Obtener el valor del microchip
                val fechaVacunaNueva = fechaVacuna.text.toString()
                val fechaDespNueva = fechaDesp.text.toString()

                // Crear un objeto Mascota con los datos actualizados
                val mascotaActualizada = Mascota(
                    id = mascota?.id ?: 0,
                    usuario = mascota.usuario,
                    nombre = mascota?.nombre ?: "",
                    raza = mascota.raza,
                    edad = mascota.edad,
                    fechaNacimiento = mascota.fechaNacimiento,
                    peso = pesoNuevo.toDoubleOrNull() ?: mascota?.peso ?: 0.0,
                    genero = mascota?.genero ?: "",
                    esterilizado = mascota.esterilizado,
                    fechaProximaVacunacion = fechaVacunaNueva,
                    fechaProximaDesparasitacion = fechaDespNueva,
                    foto = mascota?.foto ?: "",
                    metrosRecorridos = mascota?.metrosRecorridos ?: 0L,
                    pienso = piensoNuevo, // nuevo campo
                    microchip = microchipNuevo // nuevo campo
                )

                // Realizar la llamada a la API para actualizar la mascota
                actualizarMascotaAPI(mascotaActualizada)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }


    private fun actualizarMascotaAPI(mascotaEditar: Mascota) {
        // Aquí usaríamos Retrofit para hacer la petición PUT a la API
        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.170.200:8080/dogidog/") // Cambia esta URL por la de tu servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        // Hacemos la llamada PUT
        val call = service.actualizarMascota(mascotaEditar.id, mascotaEditar)
        call.enqueue(object : Callback<Mascota> {
            override fun onResponse(call: Call<Mascota>, response: Response<Mascota>) {
                if (response.isSuccessful) {
                    val mascotaActualizada = response.body()
                    if (mascotaActualizada != null) {
                        mascota = mascotaActualizada
                        actualizarUI(mascotaActualizada)
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al actualizar la mascota", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Mascota>, t: Throwable) {
                Toast.makeText(requireContext(), "Fallo en la conexión", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun actualizarUI(mascota: Mascota) {
        binding.numeroMicrochip.text = mascota.microchip
        binding.txtNumEdad.text = "${mascota.edad ?: "?"} años"
        binding.txtNumPeso.text = "${mascota.peso ?: "?"} kg"
        binding.txtNumPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.primario))
        binding.txtProblemaPeso.isVisible = false
        if(mascota.genero.equals("Hembra")){
            if(mascota.peso!! < mascota.raza.pesoMinHembra){
                binding.txtNumPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                binding.txtNumPeso.text = "${mascota.peso ?: "?"} kg"
                binding.txtProblemaPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                binding.txtProblemaPeso.text = "(Infrapeso)"
                binding.txtProblemaPeso.isVisible = true
            }else if (mascota.peso!! > mascota.raza.pesoMaxHembra){
                binding.txtNumPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                binding.txtNumPeso.text = "${mascota.peso ?: "?"} kg "
                binding.txtProblemaPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                binding.txtProblemaPeso.text = "(Sobrepeso)"
                binding.txtProblemaPeso.isVisible = true
            }
        }else if(mascota.genero.equals("Macho")){
            if(mascota.peso!! < mascota.raza.pesoMinMacho){
                binding.txtNumPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                binding.txtNumPeso.text = "${mascota.peso ?: "?"} kg"
                binding.txtProblemaPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                binding.txtProblemaPeso.text = "(Infrapeso)"
                binding.txtProblemaPeso.isVisible = true
            }else if (mascota.peso!! > mascota.raza.pesoMaxMacho){
                binding.txtNumPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                binding.txtNumPeso.text = "${mascota.peso ?: "?"} kg"
                binding.txtProblemaPeso.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                binding.txtProblemaPeso.text = "(Sobrepeso)"
                binding.txtProblemaPeso.isVisible = true
            }
        }
        val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val fechaActual = Date()

        val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // formato original
        val formatoSalida = SimpleDateFormat("dd-MM-yyyy", Locale("es", "ES")) // formato español

        binding.txtFechaVacuna.text = mascota.fechaProximaVacunacion?.let {
            try {
                val fecha = formatoEntrada.parse(it)
                if (fecha != null) formatoSalida.format(fecha) else getString(R.string.sinInfo)
            } catch (e: ParseException) {
                getString(R.string.sinInfo)
            }
        } ?: getString(R.string.sinInfo)

        binding.txtFechaDesparasitacion.text = mascota.fechaProximaDesparasitacion?.let {
            try {
                val fecha = formatoEntrada.parse(it)
                if (fecha != null) formatoSalida.format(fecha) else getString(R.string.sinInfo)
            } catch (e: ParseException) {
                getString(R.string.sinInfo)
            }
        } ?: getString(R.string.sinInfo)
        binding.txtFechaVacuna.setTextColor(ContextCompat.getColor(requireContext(), R.color.primario))
// Verificamos y coloreamos la fecha de vacunación
        mascota.fechaProximaVacunacion?.let { fechaStr ->
            try {
                val fechaVacuna = formatoFecha.parse(fechaStr)
                if (fechaVacuna != null && fechaVacuna.before(fechaActual)) {
                    binding.txtFechaVacuna.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        binding.txtFechaDesparasitacion.setTextColor(ContextCompat.getColor(requireContext(), R.color.primario))
// Verificamos y coloreamos la fecha de desparasitación
        mascota.fechaProximaDesparasitacion?.let { fechaStr ->
            try {
                val fechaDesparasitacion = formatoFecha.parse(fechaStr)
                if (fechaDesparasitacion != null && fechaDesparasitacion.before(fechaActual)) {
                    binding.txtFechaDesparasitacion.setTextColor(ContextCompat.getColor(requireContext(), R.color.rojo))
                }
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        binding.txtMarcaPienso.text = mascota.pienso ?: getString(R.string.sinInfo)
    }

}