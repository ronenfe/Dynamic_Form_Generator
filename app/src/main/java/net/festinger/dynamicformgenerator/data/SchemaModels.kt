package net.festinger.dynamicformgenerator.data

enum class FieldType {
    STRING, NUMBER, BOOLEAN, ENUM_DROPDOWN, DATE, TIME, MULTI_SELECT, GPS_LOCATION, IMAGE
}

data class FormFieldSchema(
    val key: String,
    val type: FieldType,
    val label: String,
    val required: Boolean = false,
    val min: Double? = null,
    val max: Double? = null,
    val regex: String? = null,
    val options: List<String>? = null,
    val uiWidget: String? = null,
    val uiPlaceholder: String? = null,
    val uiAutofocus: Boolean = false
)
