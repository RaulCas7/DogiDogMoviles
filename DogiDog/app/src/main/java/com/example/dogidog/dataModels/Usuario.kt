package com.example.dogidog.dataModels

data class Usuario(
    val usuario: String,
    val email: String,
    val password: String,
    val contadorPreguntas: Int = 0
)