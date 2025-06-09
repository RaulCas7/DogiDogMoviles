package com.example.dogidog.mascotas

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.dogidog.R
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Documentacion
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
import java.io.FileOutputStream
import java.io.InputStream
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
            try {
                uri?.let {
                    archivoSeleccionadoUri = it
                    Log.d("CrearDocumento", "Archivo seleccionado URI: $it")
                    val nombreArchivo = obtenerNombreArchivoDesdeUri(it)
                    binding.tvNombreArchivo.text = nombreArchivo
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error al seleccionar archivo: ${e.message}", Toast.LENGTH_LONG).show()
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

        val documentacionJson = DocumentacionCrear(
            mascota = mascota,
            tipo = tipo,
            fecha = fechaFormateada,
            descripcion = descripcion,
            archivo = archivoUri?.lastPathSegment ?: "", // Si no hay archivo, ponemos una cadena vacía
            creadoEn = LocalDateTime.now().toString()
        )

        // Convertir el objeto DocumentacionCrear a JSON con Gson
        val gson = Gson()
        val documentacionJsonString = gson.toJson(documentacionJson)

        // Crear RequestBody con el JSON
        val documentacionRequestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(), documentacionJsonString
        )

        // Configurar Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.170.200:8080/dogidog/") // Cambia esta URL por la de tu servidor
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.guardarDocumentacionSinArchivo(documentacionJson).enqueue(object : Callback<DocumentacionCrear> {
            override fun onResponse(call: Call<DocumentacionCrear>, response: Response<DocumentacionCrear>) {
                if (response.isSuccessful) {
                    val documentoCreado = response.body()
                    val idDocumento = documentoCreado?.id  // Obtener el ID del documento recién creado
                    if (idDocumento != null) {
                        // Si hay archivo, actualizamos con el archivo
                        archivoUri?.let {
                            actualizarArchivoDocumentacion(idDocumento, it)
                        } ?: run {
                            // Si no hay archivo, podemos cerrar el fragmento directamente
                            Toast.makeText(requireContext(), "Documento creado sin archivo", Toast.LENGTH_SHORT).show()
                            navigateBackToPreviousFragment()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<DocumentacionCrear>, t: Throwable) {
                Toast.makeText(requireContext(), "Error en la conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarArchivoDocumentacion(idDocumento: Int, archivoUri: Uri) {
        // Obtener el nombre del archivo desde la URI
        val nombreArchivo = obtenerNombreArchivoDesdeUri(archivoUri)

        // Obtener el archivo de la URI y usar el nombre real
        val file = obtenerArchivoDesdeUri(archivoUri, nombreArchivo)

        file?.let {
            val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), it)
            val archivoPart = MultipartBody.Part.createFormData("fichero", it.name, requestFile)

            // Configurar Retrofit para la segunda llamada PUT
            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.170.200:8080/dogidog/") // Cambia esta URL por la de tu servidor
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(ApiService::class.java)

            // Llamada PUT para asociar el archivo al documento
            service.actualizarArchivoDocumentacion(idDocumento, archivoPart).enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Archivo asociado al documento con ID $idDocumento", Toast.LENGTH_SHORT).show()

                        navigateBackToPreviousFragment()
                    } else {
                        Toast.makeText(requireContext(), "Error al asociar el archivo: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Toast.makeText(requireContext(), "Subido archivo correctamente", Toast.LENGTH_SHORT).show()
                }
            })
        } ?: run {
            Toast.makeText(requireContext(), "Error al obtener el archivo.", Toast.LENGTH_SHORT).show()
        }
    }

    // Método para obtener el archivo físico desde la URI
    fun obtenerArchivoDesdeUri(uri: Uri, nombreArchivo: String): File? {
        val contentResolver = context?.contentResolver
        val file = File(context?.cacheDir, nombreArchivo) // Usamos el nombre real del archivo

        try {
            // Abrir el InputStream desde la URI
            val inputStream: InputStream? = contentResolver?.openInputStream(uri)
            // Crear un OutputStream en el archivo temporal
            val outputStream = FileOutputStream(file)

            // Copiar los datos del InputStream al OutputStream
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            return file // Devolver el archivo creado en el almacenamiento interno
        } catch (e: Exception) {
            e.printStackTrace() // Imprimir el error si ocurre algún fallo
            return null // Devolver null si ocurre un error
        }
    }

    private fun obtenerNombreArchivoDesdeUri(uri: Uri): String {
        var nombreArchivo = "Archivo seleccionado"
        try {
            val cursor = context?.contentResolver?.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (columnIndex != -1) {
                        nombreArchivo = it.getString(columnIndex)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return nombreArchivo
    }


    private fun navigateBackToPreviousFragment() {
        // Utilizamos el FragmentManager para hacer el "pop" y volver al fragmento anterior
        requireActivity().supportFragmentManager.popBackStack()
    }
}
