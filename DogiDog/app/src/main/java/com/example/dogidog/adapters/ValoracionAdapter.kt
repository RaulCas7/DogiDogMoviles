package com.example.dogidog.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.dogidog.R
import com.example.dogidog.dataModels.Valoracion

class ValoracionAdapter(
    private val context: Context,
    private val valoraciones: List<Valoracion>
) : ArrayAdapter<Valoracion>(context, 0, valoraciones) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_valoracion, parent, false)
        val valoracion = valoraciones[position]

        val tvUsuario = view.findViewById<TextView>(R.id.tvUsuario)
        val tvComentario = view.findViewById<TextView>(R.id.tvComentario)
        val llStars = view.findViewById<LinearLayout>(R.id.llStars)

        tvUsuario.text = valoracion.usuario.usuario
        tvComentario.text = valoracion.comentario

        // Limpiar y añadir estrellas dinámicamente
        llStars.removeAllViews()
        for (i in 1..5) {
            val star = ImageView(context)
            star.setImageResource(if (i <= valoracion.puntuacion) R.drawable.star_filled else R.drawable.star_empty)
            val params = LinearLayout.LayoutParams(48, 48)
            params.marginEnd = 4
            star.layoutParams = params
            llStars.addView(star)
        }

        return view
    }
}