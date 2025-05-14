package com.example.dogidog.adapters

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.dogidog.R
import com.example.dogidog.apiServices.ApiService
import com.example.dogidog.dataModels.Documentacion
import com.example.dogidog.databinding.ItemDocumentBinding
import com.google.common.net.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DocumentosAdapter(
    private val context: Context,
    private var documentos: List<Documentacion>
) : RecyclerView.Adapter<DocumentosAdapter.DocumentoViewHolder>() {

    private var documentosFiltrados: List<Documentacion> = documentos

    inner class DocumentoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tipoIcon: ImageView = view.findViewById(R.id.imgTipoDocumento)
        val titulo: TextView = view.findViewById(R.id.tvTituloDocumento)
        val descripcion: TextView = view.findViewById(R.id.tvDescripcionDocumento)
        val fecha: TextView = view.findViewById(R.id.tvFechaDocumento)
        val contenedorArchivos: LinearLayout = view.findViewById(R.id.linearArchivosAdjuntos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_document, parent, false)
        return DocumentoViewHolder(view)
    }

    // Método para actualizar la lista de documentos
    fun updateData(newDocumentos: List<Documentacion>) {
        documentos = newDocumentos
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: DocumentoViewHolder, position: Int) {
        val doc = documentosFiltrados[position]

        holder.titulo.text = doc.tipo
        holder.descripcion.text = doc.descripcion ?: ""
        holder.fecha.text = doc.fecha

        // Icono por tipo
        holder.tipoIcon.setImageResource(
            when (doc.tipo.lowercase()) {
                "consulta" -> R.drawable.clinica
                "vacuna realizada" -> R.drawable.vacuna
                "desparasitación" -> R.drawable.cuentagotas
                "esterilización" -> R.drawable.esterelizacion
                "cambio de pienso" -> R.drawable.alimentos_para_mascotas
                "cartilla" -> R.drawable.tarjeta
                "otros" -> R.drawable.hueso
                else -> R.drawable.hueso
            }
        )
        holder.tipoIcon.setColorFilter(ContextCompat.getColor(context, R.color.primario), PorterDuff.Mode.SRC_IN)

        // Limpiar cualquier contenido anterior
        holder.contenedorArchivos.removeAllViews()

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context).apply {
                setTitle("Eliminar documento")
                setMessage("¿Desea borrar el documento \"${doc.tipo}\"?")
                setPositiveButton("Sí") { _, _ ->
                    eliminarDocumento(doc.id, position)
                }
                setNegativeButton("Cancelar", null)
                show()
            }
            true // importante para que se consuma el evento
        }

        if (!doc.archivo.isNullOrBlank()) {
            val verArchivosText = TextView(holder.itemView.context).apply {
                text = "Ver Archivos"
                setTextColor(ContextCompat.getColor(context, R.color.primario))
                textSize = 14f
                setPadding(0, 8, 0, 0)
                setOnClickListener {
                    mostrarArchivos(doc.archivo, holder)
                }
            }
            holder.contenedorArchivos.addView(verArchivosText)
            holder.contenedorArchivos.visibility = View.VISIBLE
        } else {
            holder.contenedorArchivos.visibility = View.GONE
        }
    }

    private fun mostrarArchivos(archivoStr: String?, holder: DocumentoViewHolder) {
        holder.contenedorArchivos.removeAllViews()

        if (!archivoStr.isNullOrEmpty()) {
            val archivos = archivoStr.split(",")
            archivos.forEach { archivoUrl ->
                val archivoNombre = archivoUrl.substringAfterLast("/")

                val archivoLayout = LinearLayout(holder.itemView.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(0, 4, 0, 4)
                    gravity = Gravity.CENTER_VERTICAL
                }

                val tvArchivo = TextView(holder.itemView.context).apply {
                    text = archivoNombre
                    setTextColor(ContextCompat.getColor(context, R.color.primario))
                    textSize = 14f
                }

                val imgDescargar = ImageView(holder.itemView.context).apply {
                    setImageResource(R.drawable.baseline_download_24)
                    layoutParams = LinearLayout.LayoutParams(48, 48)
                    setPadding(16, 0, 0, 0)
                    setOnClickListener {
                        // Obtén el id del documento al que pertenece el archivo
                        val idDocumento = documentosFiltrados[holder.adapterPosition].id
                        descargarArchivo(idDocumento)
                    }
                }

                archivoLayout.addView(tvArchivo)
                archivoLayout.addView(imgDescargar)
                holder.contenedorArchivos.addView(archivoLayout)
            }
        } else {
            val tvNoArchivos = TextView(holder.itemView.context).apply {
                text = "No hay archivos"
                setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                textSize = 14f
            }
            holder.contenedorArchivos.addView(tvNoArchivos)
        }
    }

    private fun descargarArchivo(idDocumento: Int) {
        // Configuramos Retrofit para descargar el archivo
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Cambia esta URL por la de tu servidor
            .addConverterFactory(GsonConverterFactory.create()) // Convierte las respuestas
            .build()

        val service = retrofit.create(ApiService::class.java)

        // Creamos la notificación de descarga
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val channelId = "descarga_archivo"

        // Verifica la versión de Android para crear el canal de notificación en versiones >= 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Descarga", NotificationManager.IMPORTANCE_LOW)
            notificationManager?.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Descargando archivo")
            .setContentText("Por favor espera...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(0, 0, true) // Mostrar el progreso de la descarga (indeterminado por ahora)
            .setOngoing(true)

        notificationManager?.notify(1, notificationBuilder.build())

        // Hacemos la llamada para descargar el archivo
        service.descargarArchivoDocumentacion(idDocumento).enqueue(object : Callback<ResponseBody> {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("DESCARGA", "Código HTTP: ${response.code()}")
                Log.d("DESCARGA", "Es exitosa: ${response.isSuccessful}")
                Log.d("DESCARGA", "Headers: ${response.headers()}")

                if (response.isSuccessful) {
                    val contentDisposition = response.headers().get(HttpHeaders.CONTENT_DISPOSITION)
                    val filename = obtenerNombreArchivoDesdeHeader(contentDisposition)
                    Log.d("DESCARGA", "Nombre del archivo: $filename")

                    response.body()?.let { body ->
                        guardarArchivoLocalmente(body, filename, notificationManager)
                    } ?: run {
                        Log.e("DESCARGA", "Body nulo")
                    }
                } else {
                    Log.e("DESCARGA", "Fallo: ${response.code()} - ${response.message()}")
                    Toast.makeText(context, "Error al descargar el archivo", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Error en la descarga: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun obtenerNombreArchivoDesdeHeader(contentDisposition: String?): String {
        var filename = "archivo_descargado"  // Nombre por defecto
        contentDisposition?.let {
            // Buscamos el nombre del archivo dentro de "attachment; filename="nombre_del_archivo"
            val regex = """filename="([^"]+)"""".toRegex()
            val matchResult = regex.find(it)
            filename = matchResult?.groupValues?.get(1) ?: filename
        }
        return filename
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun guardarArchivoLocalmente(body: ResponseBody, nombreArchivo: String, notificationManager: NotificationManager?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mimeType = "application/pdf" // Ajusta según el tipo real del archivo
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                val fileUri = resolver.insert(collection, contentValues)
                    ?: throw IOException("No se pudo crear el archivo")

                resolver.openOutputStream(fileUri)?.use { outputStream ->
                    body.byteStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                // Marcar como disponible
                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(fileUri, contentValues, null, null)

                withContext(Dispatchers.Main) {
                    // Notificación
                    val notification = NotificationCompat.Builder(context, "descarga_archivo")
                        .setContentTitle("Descarga completada")
                        .setContentText("Archivo guardado en Descargas.")
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setAutoCancel(true)
                        .build()

                    notificationManager?.notify(1, notification)

                    Toast.makeText(context, "Archivo descargado correctamente.", Toast.LENGTH_LONG).show()

                    // ✅ Abrir automáticamente
                    abrirArchivo(fileUri, mimeType)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    val notification = NotificationCompat.Builder(context, "descarga_archivo")
                        .setContentTitle("Error en la descarga")
                        .setContentText("No se pudo guardar el archivo.")
                        .setSmallIcon(android.R.drawable.stat_notify_error)
                        .setAutoCancel(true)
                        .build()

                    notificationManager?.notify(1, notification)
                    Toast.makeText(context, "Error al guardar el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun abrirArchivo(uri: Uri, mimeType: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No se pudo abrir el archivo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }










    override fun getItemCount(): Int = documentosFiltrados.size

    fun actualizarLista(nuevaLista: List<Documentacion>) {
        documentosFiltrados = nuevaLista
        notifyDataSetChanged()
    }

    private fun eliminarDocumento(id: Int, position: Int) {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        service.eliminarDocumentacion(id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Documento eliminado", Toast.LENGTH_SHORT).show()
                    documentos = documentos.filterIndexed { index, _ -> index != position }
                    documentosFiltrados = documentos
                    notifyItemRemoved(position)
                } else {
                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(context, "Fallo al conectar", Toast.LENGTH_SHORT).show()
            }
        })
    }
}