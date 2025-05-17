package com.example.dogidog.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.dogidog.dataModels.Notificacion
import com.example.dogidog.R
import com.example.dogidog.apiServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificacionesAdapter(
    //Se queda en mutable para borrar todas
    var listaNotificaciones: MutableList<Notificacion>,
    private val onSelectionModeChanged: (Boolean) -> Unit, // Llamada cuando cambia el modo de selección
    private val onClick: (Notificacion) -> Unit // Llamada cuando se hace clic en una notificación
) : RecyclerView.Adapter<NotificacionesAdapter.NotificacionViewHolder>() {

    private val notificacionesClickeadas = mutableSetOf<Int>()
    private val seleccionadas = mutableSetOf<Notificacion>()
    internal var enModoSeleccion = false

    inner class NotificacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imgNotificacion)
        val titulo: TextView = itemView.findViewById(R.id.txtTitulo)
        val descripcion: TextView = itemView.findViewById(R.id.txtDescripcion)
        val fechaCreacion: TextView = itemView.findViewById(R.id.txtFechaCreacion)
        val checkSeleccion: CheckBox = itemView.findViewById(R.id.checkSeleccion) // Nuevo checkbox

        fun bind(notificacion: Notificacion) {
            titulo.text = notificacion.titulo
            descripcion.text = notificacion.mensaje
            val fechaFormateada = formatearFecha(notificacion.fechaCreacion)
            fechaCreacion.text = fechaFormateada

            val estaPulsada = notificacionesClickeadas.contains(adapterPosition)

            itemView.setBackgroundColor(
                if (estaPulsada) Color.parseColor("#E0E0E0")  // Gris claro
                else Color.TRANSPARENT // Fondo normal
            )



            if (estaPulsada) {
                imagen.setImageResource(R.drawable.baseline_mark_email_read_24)
            } else {
                imagen.setImageResource(R.drawable.baseline_mark_email_unread_24)
            }

            if(notificacion.leida){
                imagen.setImageResource(R.drawable.baseline_mark_email_read_24)
            } else {
                imagen.setImageResource(R.drawable.baseline_mark_email_unread_24)
            }

            checkSeleccion.visibility = if (enModoSeleccion) View.VISIBLE else View.GONE
            checkSeleccion.isChecked = seleccionadas.contains(notificacion)

            itemView.setOnClickListener {

                if (enModoSeleccion) {
                    if (seleccionadas.contains(notificacion)) {
                        seleccionadas.remove(notificacion)
                    } else {
                        seleccionadas.add(notificacion)
                    }


                    notifyItemChanged(adapterPosition)

                    if (seleccionadas.isEmpty()) {
                        enModoSeleccion = false
                        onSelectionModeChanged(false)
                        notifyDataSetChanged()
                    }
                } else {
                    if (estaPulsada) {
                        notificacionesClickeadas.remove(adapterPosition)
                    } else {
                        notificacionesClickeadas.add(adapterPosition)
                    }
                    notifyItemChanged(adapterPosition)
                    onClick(notificacion)
                }
            }

            itemView.setOnLongClickListener {

                if (!enModoSeleccion) {
                    enModoSeleccion = true
                    seleccionadas.add(notificacion)
                    onSelectionModeChanged(true)
                    notifyDataSetChanged()
                }

                true
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

    fun actualizarLista(nuevaLista: MutableList<Notificacion>) {

        listaNotificaciones.clear()
        listaNotificaciones.addAll(nuevaLista)

        notifyDataSetChanged()
    }

    fun obtenerSeleccionadas(): List<Notificacion> {
        return seleccionadas.toList()
    }

    fun eliminarSeleccionadas() {
        Log.d("ADAPTER", "Intentando eliminar ${seleccionadas.size} notificaciones seleccionadas: $seleccionadas")

        if (seleccionadas.isEmpty()) {
            Log.d("ADAPTER", "⚠️ No hay notificaciones seleccionadas para eliminar.")
            return
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.26:8080/dogidog/") // Asegúrate de poner tu URL real
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        seleccionadas.forEach { notificacion ->
            apiService.eliminarNotificacion(notificacion.id).enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("ADAPTER", "✅ Notificación ${notificacion.id} eliminada del servidor.")
                    } else {
                        Log.e("ADAPTER", "❌ Falló al eliminar la notificación ${notificacion.id} (código: ${response.code()})")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("ADAPTER", "❌ Error de red al eliminar notificación ${notificacion.id}: ${t.message}")
                }
            })
        }

        // Elimina localmente después de disparar las peticiones
        listaNotificaciones.removeAll(seleccionadas)
        seleccionadas.clear()
        enModoSeleccion = false
        onSelectionModeChanged(false)
        notifyDataSetChanged()
    }


    fun formatearFecha(fechaCreacion: String): String {
        val instant = Instant.parse(fechaCreacion)
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        return formatter.format(instant)
    }

}


