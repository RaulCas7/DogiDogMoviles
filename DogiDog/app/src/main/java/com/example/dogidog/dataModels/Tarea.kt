package com.example.dogidog.dataModels

data class Tarea(
    val titulo: String?,
    val descripcion: String?,
    val fecha_creacion: String?, // formato "yyyy-MM-dd HH:mm:ss"
    val fecha_vencimiento: String? = null,
    val prioridad: String? = "Media",
    val estado: String? = "Pendiente",
    val id_empleado: Int? = null
)