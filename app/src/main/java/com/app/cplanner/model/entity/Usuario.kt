package com.app.cplanner.model.entity

data class Usuario(
    var id: String = "",
    var nombre: String = "",
    var email: String = "",
    var contrasena: String = "",
    var fotoUrl: String = ""
) {
    /**
     * Convierte este Usuario a un Map para Firestore
     */
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "nombre" to nombre,
        "email" to email,
        "contrasena" to contrasena,
        "fotoUrl" to fotoUrl
    )

    companion object {
        /**
         * Crea un Usuario a partir de un Map<String,Any?>, p. ej. DocumentSnapshot.data
         */
        fun fromMap(map: Map<String, Any?>): Usuario {
            return Usuario(
                id         = (map["id"]         as? String).orEmpty(),
                nombre     = (map["nombre"]     as? String).orEmpty(),
                email      = (map["email"]      as? String).orEmpty(),
                contrasena = (map["contrasena"] as? String).orEmpty(),
                fotoUrl    = (map["fotoUrl"]    as? String).orEmpty()
            )
        }
    }
}
