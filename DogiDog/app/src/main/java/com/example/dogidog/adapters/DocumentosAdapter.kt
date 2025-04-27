package com.example.dogidog.adapters

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.dogidog.R
import com.example.dogidog.dataModels.Documentacion
import com.example.dogidog.databinding.ItemDocumentBinding
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

        // Botón para desplegar archivos
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
                        abrirArchivo(archivoUrl)
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

    private fun abrirArchivo(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(url), "*/*")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    override fun getItemCount(): Int = documentosFiltrados.size

    fun actualizarLista(nuevaLista: List<Documentacion>) {
        documentosFiltrados = nuevaLista
        notifyDataSetChanged()
    }
}