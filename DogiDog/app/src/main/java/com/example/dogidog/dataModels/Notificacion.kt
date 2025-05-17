package com.example.dogidog.dataModels

import java.time.Instant

data class Notificacion (
    val id: Int,
    val usuario: Usuario,
    val mascotaId: Int?,
    val titulo: String,
    val mensaje: String,
    val fechaCreacion: String,
    var leida: Boolean
)
