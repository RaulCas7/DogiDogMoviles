package com.example.dogidog.mascotas

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.dogidog.adapters.FotosAdapter
import com.example.dogidog.dataModels.Foto
import com.example.dogidog.databinding.FragmentGaleriaMascotaBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MascotaGaleriaFragment : Fragment() {

    private lateinit var binding: FragmentGaleriaMascotaBinding
    private val fotos = mutableListOf<Foto>()
    private lateinit var adapter: FotosAdapter
    private val REQUEST_TAKE_PHOTO = 1
    private var photoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGaleriaMascotaBinding.inflate(inflater, container, false)

        // Configurar el RecyclerView
        adapter = FotosAdapter(fotos)
        binding.rvGaleria.adapter = adapter

        // Configurar el botón flotante para tomar fotos
        binding.btnHacerFoto.setOnClickListener {
            if (checkPermissions()) {
                abrirCamara()
            }
        }
        // Llamar a la función para cargar las fotos
        loadFotos()

        return binding.root
    }

    // Cargar fotos (puedes cargar desde almacenamiento o base de datos)
    private fun loadFotos() {
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val files = storageDir?.listFiles()

        fotos.clear()
        files?.forEach { file ->
            val uri = Uri.fromFile(file)
            fotos.add(Foto(uri, file.lastModified()))
        }
        adapter.notifyDataSetChanged()
    }

    // Crear un archivo para la foto (se guarda en el almacenamiento externo)

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        if (storageDir == null || !storageDir.exists()) {
            storageDir?.mkdirs() // Crea la carpeta si no existe
        }

        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir).apply {
            photoUri = Uri.fromFile(this)
        }
    }

    // Método que reemplaza `onActivityResult` (para Android 11 y superior)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            photoUri?.let {
                Log.d("Foto", "Foto guardada en: ${it.toString()}")
                fotos.add(Foto(it, System.currentTimeMillis()))
                adapter.notifyDataSetChanged()
            } ?: Log.e("Error", "photoUri es null")
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA)
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        return if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), 1)
            false
        } else {
            true
        }
    }
    private fun abrirCamara() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            val photoFile = createImageFile()
            photoFile?.let {
                photoUri = FileProvider.getUriForFile(requireContext(), "com.example.dogidog.fileprovider", it)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                startActivityForResult(intent, REQUEST_TAKE_PHOTO)
            }
        } else {
            Toast.makeText(requireContext(), "No se encontró una app de cámara", Toast.LENGTH_SHORT).show()
        }
    }
}



