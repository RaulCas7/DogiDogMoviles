package com.example.dogidog.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogidog.dataModels.Notificacion
import com.example.dogidog.R
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificacionesAdapter(
    var listaNotificaciones: List<Notificacion>,
    private val onClick: (Notificacion) -> Unit
) : RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder>() {

    private val notificacionesClickeadas = mutableSetOf<Int>()
    inner class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imgNotificacion)
        val titulo: TextView = itemView.findViewById(R.id.txtTitulo)
        val descripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
        val fechaCreacion: TextView = itemView.findViewById(R.id.txtFechaCreacion)

        fun bind(notificacion: Notificacion) {
            titulo.text = notificacion.titulo
            descripcion.text = notificacion.mensaje
            val fechaFormateada = formatearFecha(notificacion.fechaCreacion)
            fechaCreacion.text = fechaFormateada

                val estaPulsada = notificacionesClickeadas.contains(adapterPosition)

            // **Cambia el color de fondo**
            itemView.setBackgroundColor(
                if (estaPulsada) Color.parseColor("#E0E0E0")  // Gris claro
                else Color.TRANSPARENT // Fondo normal
            )

            // Asegurar que se actualiza correctamente la imagen
                if (estaPulsada) {
                    imagen.setImageResource(R.drawable.baseline_mark_email_read_24)
                } else {
                    imagen.setImageResource(R.drawable.baseline_mark_email_unread_24)
                }

                itemView.setOnClickListener {
                    if (estaPulsada) {
                        notificacionesClickeadas.remove(adapterPosition)
                    } else {
                        notificacionesClickeadas.add(adapterPosition)
                    }

                    // Forzar actualización del ítem
                    notifyItemChanged(adapterPosition)

                    onClick(notificacion)
                }
            }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notificacion, parent, false)
        return NotificacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificacionViewHolder, position: Int) {
        holder.bind(listaNotificaciones[position])
    }

    override fun getItemCount() = listaNotificaciones.size

    fun actualizarLista(nuevaLista: List<Notificacion>) {
        listaNotificaciones = nuevaLista
        notifyDataSetChanged()
    }

    fun formatearFecha(fechaCreacion: String): String {
        // Convertir el String a Instant
        val instant = Instant.parse(fechaCreacion)

        // Crear el formateador de fecha y hora (por ejemplo: "dd/MM/yyyy HH:mm")
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())

        // Convertir el Instant a una cadena formateada
        return formatter.format(instant)
    }
}


