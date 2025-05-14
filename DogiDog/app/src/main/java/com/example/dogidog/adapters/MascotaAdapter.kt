package com.example.dogidog.adapters

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.dogidog.R
import com.example.dogidog.dataModels.Mascota

class MascotaAdapter(
    var listaMascotas: List<Mascota>,
    private val onClick: (Mascota) -> Unit
) : RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    inner class MascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.image)
        val nombre: TextView = itemView.findViewById(R.id.item_number)
        val raza: TextView = itemView.findViewById(R.id.content)
        val edad: TextView = itemView.findViewById(R.id.raza)
        val peso: TextView = itemView.findViewById(R.id.peso)

        fun bind(mascota: Mascota) {
            nombre.text = mascota.nombre
            raza.text = mascota.raza.nombre
            edad.text = "${mascota.edad} años"
            peso.text = "${mascota.peso} kg"

            // Usar fotoBitmap si está disponible, si no, cargar la imagen desde la URL
            if (mascota.fotoBitmap != null) {
                val bitmapRedondeado = redondearEsquinas(mascota.fotoBitmap!!, 100f) // Radio de 16dp
                imagen.setImageBitmap(bitmapRedondeado)

            } else {
                // Si no hay Bitmap, intentar cargar la foto desde la URL
                Glide.with(itemView.context)
                    .load(mascota.foto)
                    .placeholder(
                        if (mascota.genero == "Macho") R.drawable.bordercollie
                        else R.drawable.borderhembra) // Imagen por defecto
                    .apply(RequestOptions().transform(RoundedCorners(100)))
                    .into(imagen)
            }

            itemView.setOnClickListener { onClick(mascota) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mascota, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        holder.bind(listaMascotas[position])
    }

    override fun getItemCount() = listaMascotas.size

    fun actualizarLista(nuevaLista: List<Mascota>) {
        listaMascotas = nuevaLista
        notifyDataSetChanged()
    }
    fun redondearEsquinas(bitmap: Bitmap, radio: Float): Bitmap {
        // Crear un bitmap mutable para el recorte
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        // Crear un canvas para dibujar en el bitmap
        val canvas = Canvas(output)

        // Crear un paint para controlar el dibujo
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        // Crear un rectángulo con bordes redondeados
        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        canvas.drawRoundRect(rect, radio, radio, paint)

        return output
    }
}
