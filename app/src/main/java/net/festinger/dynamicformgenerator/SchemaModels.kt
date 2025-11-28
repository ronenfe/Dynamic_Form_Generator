package net.festinger.dynamicformgenerator

enum class FieldType {
    STRING, NUMBER, BOOLEAN, ENUM_DROPDOWN, DATE, TIME, MULTI_SELECT, IMAGE, GPS_LOCATION
}

data class FormFieldSchema(
    val key: String,
    val label: String,
    val type: FieldType,
    val required: Boolean = false,
    val options: List<String>? = null,
    val uiOptions: Map<String, Any> = emptyMap(),
    // VALIDATION FIELDS
    val min: Double? = null,
    val max: Double? = null,
    val regex: String? = null
)
