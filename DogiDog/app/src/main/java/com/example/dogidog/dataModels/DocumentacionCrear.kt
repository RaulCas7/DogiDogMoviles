package com.example.dogidog.dataModels

data class DocumentacionCrear(
    val id: Int? = null,          // El ID es opcional, ya que lo generará el servidor al crear el documento
    val mascota: Mascota,        // Asegúrate de tener el modelo Mascota
    val tipo: String,
    val fecha: String,
    val descripcion: String?,
    val archivo: String?,
    val creadoEn: String
)
