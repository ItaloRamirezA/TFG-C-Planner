package com.app.cplanner.model.entity

data class Tarea(
    var id: String = "",

    var nombre: String = "",

) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "nombre" to nombre
        )
    }
}