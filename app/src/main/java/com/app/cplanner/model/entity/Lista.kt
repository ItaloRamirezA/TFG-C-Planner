package com.app.cplanner.model.entity

data class Lista(
    val id: String = "",
    val nombre: String,
    val categoryId: String? = null,
    val elementos: List<String> = emptyList()
)
