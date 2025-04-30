package com.example.dogidog.mascotas

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.dogidog.R
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.DocumentacionCrear
import com.example.dogidog.dataModels.Mascota
import com.example.dogidog.databinding.FragmentCrearDocumentoBinding
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar


class CrearDocumentoFragment : Fragment() {

    lateinit var binding: FragmentCrearDocumentoBinding

    private var archivoSeleccionadoUri: Uri? = null
    private lateinit var mascota: Mascota

    private val selectorArchivoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                archivoSeleccionadoUri = it
                val nombre = it.lastPathSegment?.substringAfterLast("/") ?: "Archivo seleccionado"
                binding.tvNombreArchivo.text = nombre
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCrearDocumentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        mascota = arguments?.getParcelable("mascota")!!

        // Cargar tipos
        val tipos = listOf(
            "Consulta", "Vacuna realizada", "Desparasitación",
            "Esterilización", "Cambio de pienso", "Cartilla", "Otros"
        )
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, tipos)
        binding.spinnerTipoDocumento.adapter = adapter

        // Calendario
        binding.btnCalendario.setOnClickListener {
            mostrarDatePicker()
        }

        // Adjuntar archivo
        binding.btnAdjuntarArchivo.setOnClickListener {
            selectorArchivoLauncher.launch("*/*")
        }

        // Guardar documento
        binding.btnGuardarDocumento.setOnClickListener {
            guardarDocumento()
        }
    }

    private fun mostrarDatePicker() {
        val c = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val fecha = String.format("%02d/%02d/%04d", day, month + 1, year)
                binding.etFecha.setText(fecha)
            },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun guardarDocumento() {
        val tipo = binding.spinnerTipoDocumento.selectedItem.toString()
        val descripcion = binding.etDescripcion.text.toString()
        val archivoUri = archivoSeleccionadoUri

        val fechaFormateada = LocalDate.parse(
            binding.etFecha.text.toString(),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        ).toString()

        // Crear el objeto Documentacion
        val documentacionJson = DocumentacionCrear(
            mascota = mascota,
            tipo = tipo,
            fecha = fechaFormateada,
            descripcion = descripcion,
            archivo = archivoUri?.lastPathSegment ?: "", // Si no hay archivo, ponemos una cadena vacía
            creadoEn = LocalDateTime.now().toString()
        )

        // Convertir el objeto documentacion a JSON
        val documentacionRequestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(), Gson().toJson(documentacionJson)
        )

        // Obtener el id de la mascota
        val idmascota = mascota.id  // Asegúrate de tener acceso al id de la mascota

        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Cambia esta URL por la de tu servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        // Comprobamos si hay un archivo seleccionado
        if (archivoUri != null) {
            // Si hay archivo, convertirlo a MultipartBody.Part
            val file = File(archivoUri.path ?: "")
            val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            val archivoPart = MultipartBody.Part.createFormData("archivo", archivoUri.lastPathSegment, requestFile)

            // Llamada a la API con el archivo
            service.guardarDocumentacion(mascota.id, documentacionRequestBody, archivoPart).enqueue(object : Callback<DocumentacionCrear> {
                override fun onResponse(call: Call<DocumentacionCrear>, response: Response<DocumentacionCrear>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Documento creado exitosamente", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error al crear el documento: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DocumentacionCrear>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // No hay archivo → usamos solo JSON
            val call = service.guardarDocumentacionSinArchivo(documentacionJson)
            call.enqueue(object : Callback<DocumentacionCrear> {
                    override fun onResponse(call: Call<DocumentacionCrear>, response: Response<DocumentacionCrear>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Documento creado sin archivo", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DocumentacionCrear>, t: Throwable) {
                        Toast.makeText(requireContext(), "Fallo en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
    }

}
