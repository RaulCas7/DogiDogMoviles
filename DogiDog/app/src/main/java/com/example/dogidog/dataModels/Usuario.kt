package com.example.dogidog.dataModels

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Usuario(
    val id: Int,
    var usuario: String,
    val email: String,
    var password: String,
    val contadorPreguntas: Int = 0,
    val latitud: Double?,  // latitud del usuario
    val longitud: Double?,  // longitud del usuario
    val valoracion: Int? = null,
    var foto: Int?,
    var fotoBitmap: Bitmap? = null // Agregar el Bitmap para la foto
) : Parcelable