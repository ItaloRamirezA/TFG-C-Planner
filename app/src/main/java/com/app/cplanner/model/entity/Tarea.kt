package com.app.cplanner.model.entity

data class Tarea(
    var id             : String = "",
    var titulo         : String = "",
    var reminder       : Boolean = false,
    var categoryId     : String = "",
    var colorHex       : String = "#000000",
    var multiDay       : Boolean = false,
    var date           : String = "",
    var sharedWith     : List<String> = emptyList(),  // <-- IDs de usuarios
    var attachmentUri  : String = ""
) {
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
