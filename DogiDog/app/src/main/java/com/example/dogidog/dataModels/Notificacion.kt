package com.example.dogidog.dataModels

import java.time.Instant

data class Notificacion(
    val id: Int,
    val usuario: Usuario,
    val mascotaId: Int?,
    val titulo: String,
    val mensaje: String,
    val fechaCreacion: String,
    var leida: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Notificacion) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
