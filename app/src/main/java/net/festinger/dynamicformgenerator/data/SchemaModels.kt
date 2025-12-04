package net.festinger.dynamicformgenerator.data

enum class FieldType {
    STRING, NUMBER, BOOLEAN, ENUM_DROPDOWN, DATE, TIME, MULTI_SELECT, GPS_LOCATION, IMAGE
}

data class FormFieldSchema(
    val key: String,
    val type: FieldType,
    val label: String,
    val required: Boolean = false,

    // Data schema properties
    val min: Double? = null,
    val max: Double? = null,
    val regex: String? = null,
    val options: List<String>? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val default: Any? = null,

    // UI schema properties
    val uiWidget: String? = null,
    val uiPlaceholder: String? = null,
    val uiAutofocus: Boolean = false,
    val uiHelp: String? = null,
    val uiIcon: String? = null,
    val uiLines: Int? = null,
    val uiMaxLines: Int? = null,
    val uiButtonLabel: String? = null,
    val uiStep: Double? = null,
    val uiFormat: String? = null,
    val uiInputType: String? = null
)
