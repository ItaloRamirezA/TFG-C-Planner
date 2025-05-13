package com.app.cplanner.model.entity

data class Tarea(
    var id            : String = "",
    var nombre        : String = "",     // título
    var reminder      : Boolean = false, // recordatorio on/off
    var categoryId    : String = "",     // id de categoría
    var colorHex      : String = "#FFFFFF", // color de tarea
    var multiDay      : Boolean = false, // si es multi-día
    var date          : String = "",     // fecha en formato ISO yyyy-MM-dd
    var imageUri      : String = "",     // URI de imagen opcional
    var attachmentUri : String = ""      // URI de archivo opcional
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id"            to id,
        "nombre"        to nombre,
        "reminder"      to reminder,
        "categoryId"    to categoryId,
        "colorHex"      to colorHex,
        "multiDay"      to multiDay,
        "date"          to date,
        "imageUri"      to imageUri,
        "attachmentUri" to attachmentUri
    )
}
