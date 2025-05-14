package com.example.dogidog.dataModels

import android.graphics.Bitmap

data class Logro(
    val id: Int,
    val titulo: String,
    val descripcion: String,
    val emblema: String,
    var emblemaBitmap: Bitmap?,
    val desbloqueado: Boolean,
)
