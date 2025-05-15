package com.app.cplanner.model.entity

/**
 * Clase que representa una tarea en la aplicación.
 */
data class Tarea(
    var id             : String = "",
    var titulo         : String = "",
    var reminder       : Boolean = false,
    var categoryId     : String = "",
    var colorHex       : String = "#000000",
    var multiDay       : Boolean = false,
    var date           : String = "",
    var sharedWith     : List<String> = emptyList(),
    var attachmentUri  : String = ""
) {
    /**
     * Convierte la tarea a un mapa de clave-valor para su almacenamiento en Firestore.
     *
     * @return Mapa con los atributos de la tarea.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "id"            to id,
        "titulo"        to titulo,
        "reminder"      to reminder,
        "categoryId"    to categoryId,
        "colorHex"      to colorHex,
        "multiDay"      to multiDay,
        "date"          to date,
        "sharedWith"    to sharedWith,
        "attachmentUri" to attachmentUri
    )
}
