package com.example.dogidog.dataModels

data class Valoracion(
    val puntuacion: Int,
    val comentario: String?,
    val fechaValoracion: String,  // Puede ser una cadena en formato ISO
    val usuario: Usuario,         // Información del usuario que valoró
    val valorado: Usuario         // Información del usuario que fue valorado
)
