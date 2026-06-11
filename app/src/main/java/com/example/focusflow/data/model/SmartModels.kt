package com.example.focusflow.data.model

data class RutinaSugerida(
    val nombre: String,
    val descripcion: String,
    val tareas: List<TareaSugerida>
)

data class TareaSugerida(
    val titulo: String,
    val descripcion: String,
    val duracionEstimada: String
)
