package com.app.cplanner.model.entity

/**
 * Clase que representa una tarea en la aplicación.
 */
data class Tarea(
    var id: String = "",
    var titulo: String = "",
    var reminder: Boolean = false,
    var categoryId: String = "",
    var colorHex: String = "#000000",
    var multiDay: Boolean = false,
    var startDate: String = "",
    var endDate: String = "",
    var sharedWith: List<String> = emptyList(),
    var attachments: List<String> = emptyList()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "titulo" to titulo,
        "reminder" to reminder,
        "categoryId" to categoryId,
        "colorHex" to colorHex,
        "multiDay" to multiDay,
        "startDate" to startDate,
        "endDate" to endDate,
        "sharedWith" to sharedWith,
        "attachments" to attachments,
    )
}