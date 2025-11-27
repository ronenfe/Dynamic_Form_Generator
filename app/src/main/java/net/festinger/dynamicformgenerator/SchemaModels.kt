package net.festinger.dynamicformgenerator

import java.util.Locale

enum class FieldType {
    STRING, NUMBER, BOOLEAN, ENUM_DROPDOWN, DATE, TIME, MULTI_SELECT, IMAGE, GPS_LOCATION
}data class FormFieldSchema(
    val key: String,
    val label: String,
    val type: FieldType,
    val required: Boolean = false,
    val options: List<String>? = null,
    val uiOptions: Map<String, Any> = emptyMap(),
    // NEW VALIDATION FIELDS
    val min: Double? = null,
    val max: Double? = null,
    val regex: String? = null
)

object SchemaGenerator {
    fun generateRandomSchema(): List<FormFieldSchema> {
        val fields = mutableListOf<FormFieldSchema>()

        val rawFields = listOf(
            "poleNumber", "fixtureHeight", "bulbType",
            "maintenanceDate", "technicianNotes", "sitePhoto"
        )

        rawFields.forEach { key ->
            var type = FieldType.STRING
            var options: List<String>? = null
            var required = false
            var min: Double? = null
            var max: Double? = null
            var regex: String? = null
            var uiPlaceholder: String? = null

            when (key) {
                "poleNumber" -> {
                    // Regex Validation: Must be 2 letters, hyphen, 4 digits (e.g., NY-1234)
                    required = true
                    regex = "^[A-Z]{2}-\\d{4}$"
                    uiPlaceholder = "e.g. NY-1234"
                }
                "fixtureHeight" -> {
                    // Min/Max Validation: Height in meters
                    type = FieldType.NUMBER
                    required = true
                    min = 3.0
                    max = 30.0
                    uiPlaceholder = "3.0 - 30.0 meters"
                }
                "technicianNotes" -> {
                    // Min Length Logic could be handled via Regex too
                    regex = "^.{10,}$" // At least 10 characters
                    uiPlaceholder = "Min 10 chars details..."
                }
                "bulbType" -> {
                    type = FieldType.ENUM_DROPDOWN
                    options = listOf("LED", "HPS", "Metal Halide")
                    required = true
                }
                "maintenanceDate" -> type = FieldType.DATE
                "sitePhoto" -> {
                    type = FieldType.IMAGE
                    required = true
                }
                else -> type = FieldType.STRING
            }

            val uiOptions = mutableMapOf<String, Any>()
            if (uiPlaceholder != null) uiOptions["ui:placeholder"] = uiPlaceholder
            if (key == "technicianNotes") uiOptions["ui:widget"] = "textarea"

            fields.add(
                FormFieldSchema(
                    key = key,
                    label = formatLabel(key),
                    type = type,
                    required = required,
                    options = options,
                    uiOptions = uiOptions,
                    min = min,
                    max = max,
                    regex = regex
                )
            )
        }
        return fields
    }

    private fun formatLabel(key: String): String {
        return key.replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
