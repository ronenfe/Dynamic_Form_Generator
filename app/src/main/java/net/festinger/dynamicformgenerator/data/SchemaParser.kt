package net.festinger.dynamicformgenerator.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SchemaParser {
    // Internal model matching the JSON structure
    data class RawFieldDefinition(
        val type: String,
        val label: String,
        val required: Boolean = false,
        val options: List<String>? = null,
        val min: Double? = null,
        val max: Double? = null,
        val regex: String? = null,
        val minLength: Int? = null,
        val maxLength: Int? = null,
        val default: Any? = null
    )

    fun parseSchemas(jsonSchemaString: String, uiSchemaString: String): List<FormFieldSchema> {
        val gson = Gson()

        // 1. Parse Data Schema
        val typeToken = object : TypeToken<Map<String, RawFieldDefinition>>() {}.type
        val rawSchema: Map<String, RawFieldDefinition> = gson.fromJson(jsonSchemaString, typeToken)

        // 2. Parse UI Schema
        val uiTypeToken = object : TypeToken<Map<String, Map<String, Any>>>() {}.type
        val uiSchema: Map<String, Map<String, Any>> = gson.fromJson(uiSchemaString, uiTypeToken)

        // 3. Merge
        val mergedList = mutableListOf<FormFieldSchema>()

        rawSchema.forEach { (key, definition) ->
            val fieldType = when (definition.type.lowercase()) {
                "string" -> FieldType.STRING
                "number" -> FieldType.NUMBER
                "boolean" -> FieldType.BOOLEAN
                "date" -> FieldType.DATE
                "time" -> FieldType.TIME
                "dropdown", "enum" -> FieldType.ENUM_DROPDOWN
                "multiselect" -> FieldType.MULTI_SELECT
                "image" -> FieldType.IMAGE
                "gps" -> FieldType.GPS_LOCATION
                else -> FieldType.STRING
            }

            val fieldUiOptions = uiSchema[key] ?: emptyMap()

            // Extract specific UI properties
            val uiWidget = fieldUiOptions["ui:widget"] as? String
            val uiPlaceholder = fieldUiOptions["ui:placeholder"] as? String
            val uiAutofocus = fieldUiOptions["ui:autofocus"] as? Boolean ?: false
            val uiHelp = fieldUiOptions["ui:help"] as? String
            val uiIcon = fieldUiOptions["ui:icon"] as? String
            val uiButtonLabel = fieldUiOptions["ui:buttonLabel"] as? String
            val uiInputType = fieldUiOptions["ui:inputType"] as? String
            val uiFormat = fieldUiOptions["ui:format"] as? String
            
            // Handle numeric properties (JSON numbers come as Doubles)
            val uiLines = (fieldUiOptions["ui:lines"] as? Double)?.toInt()
            val uiMaxLines = (fieldUiOptions["ui:maxLines"] as? Double)?.toInt()
            val uiStep = fieldUiOptions["ui:step"] as? Double

            mergedList.add(
                FormFieldSchema(
                    key = key,
                    label = definition.label,
                    type = fieldType,
                    required = definition.required,
                    options = definition.options,
                    min = definition.min,
                    max = definition.max,
                    regex = definition.regex,
                    minLength = definition.minLength,
                    maxLength = definition.maxLength,
                    default = definition.default,
                    uiWidget = uiWidget,
                    uiPlaceholder = uiPlaceholder,
                    uiAutofocus = uiAutofocus,
                    uiHelp = uiHelp,
                    uiIcon = uiIcon,
                    uiLines = uiLines,
                    uiMaxLines = uiMaxLines,
                    uiButtonLabel = uiButtonLabel,
                    uiStep = uiStep,
                    uiFormat = uiFormat,
                    uiInputType = uiInputType
                )
            )
        }
        return mergedList
    }
}
