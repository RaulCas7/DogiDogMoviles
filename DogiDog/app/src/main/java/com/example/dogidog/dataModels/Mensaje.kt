package com.example.dogidog.dataModels

import android.graphics.Bitmap


data class Mensaje(
    val texto: String,
    val esUsuario: Boolean, // true si el mensaje es del usuario, false si es del bot
    val imagenPerfil: Bitmap? = null // ahora soporta imagen dinámica
)
