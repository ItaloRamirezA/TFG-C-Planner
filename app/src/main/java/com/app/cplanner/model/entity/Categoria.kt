package com.app.cplanner.model.entity

data class Categoria(
    var id: String = "",
    var nombre: String = "",
    var color: String = "",

    ) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "nombre" to nombre,
            "color" to color
        )
    }

    override fun toString(): String {
        return nombre
    }
}