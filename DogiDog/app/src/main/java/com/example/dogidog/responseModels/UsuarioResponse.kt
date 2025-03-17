package com.example.dogidog.responseModels

data class UsuarioResponse(
    val id: Int,
    val usuario: String,
    val email: String,
    val password: String
)