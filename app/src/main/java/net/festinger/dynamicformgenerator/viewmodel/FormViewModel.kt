package net.festinger.dynamicformgenerator.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import net.festinger.dynamicformgenerator.data.FieldType
import net.festinger.dynamicformgenerator.data.FormFieldSchema
import net.festinger.dynamicformgenerator.data.SchemaGenerator
import net.festinger.dynamicformgenerator.data.SchemaParser

class FormViewModel : ViewModel() {

    // State: The Schema Objects (for the UI)
    var schema = mutableStateOf<List<FormFieldSchema>>(emptyList())
        private set

    // State: The Form Data
    val formData = mutableStateMapOf<String, Any?>()
    var validationErrors = mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var submissionResult = mutableStateOf<String?>(null)

    // State: Dialog visibility & Content
    var showSchemaDialog = mutableStateOf(false)
    var rawSchemaJson = mutableStateOf("")
        private set

    // Internal storage for the strings before they are parsed
    private var tempJsonSchema: String = ""
    private var tempUiSchema: String = ""

    // STEP 1: Generate the JSON strings and show the dialog (Do NOT render yet)
    fun loadRandomJson() {
        val (jsonSchema, uiSchema) = SchemaGenerator.generateRandomJsonStrings()

        // Store them internally
        tempJsonSchema = jsonSchema
        tempUiSchema = uiSchema

        // Update the display string for the dialog
        rawSchemaJson.value = "Data Schema:\n$jsonSchema\n\nUI Schema:\n$uiSchema"

        // Show the dialog immediately
        showSchemaDialog.value = true

        // Optional: Clear current form so user knows they need to click "Parse"
        schema.value = emptyList()
        formData.clear()
    }

    // STEP 2: Parse the stored strings and render the UI
    fun parseJson() {
        if (tempJsonSchema.isEmpty()) return

        try {
            val parsedList = SchemaParser.parseSchemas(tempJsonSchema, tempUiSchema)
            schema.value = parsedList
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Reset Data & Validation
        formData.clear()
        validationErrors.value = emptyMap()
        submissionResult.value = null

        // Initialize defaults
        schema.value.forEach { field ->
            if (field.default != null) {
                formData[field.key] = field.default
            } else if (field.type == FieldType.BOOLEAN) {
                formData[field.key] = false
            }
        }
    }

    fun onDataChanged(key: String, value: Any?) {
        formData[key] = value
        if (validationErrors.value.containsKey(key)) {
            val currentErrors = validationErrors.value.toMutableMap()
            currentErrors.remove(key)
            validationErrors.value = currentErrors
        }
    }

    fun submitForm() {
        if (validateForm()) {
            val gson = GsonBuilder().setPrettyPrinting().create()
            submissionResult.value = gson.toJson(formData)
        }
    }

    private fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        schema.value.forEach { field ->
            val value = formData[field.key]

            if (field.required) {
                val isEmpty = when (value) {
                    null -> true
                    is String -> value.isBlank()
                    is List<*> -> value.isEmpty()
                    else -> false
                }
                if (isEmpty) errors[field.key] = "This field is required"
            }

            if (value != null && value.toString().isNotBlank()) {
                // REGEX Validation
                if (field.regex != null && value is String) {
                    if (!value.matches(Regex(field.regex))) {
                        errors[field.key] = "Invalid format. Expected pattern: ${field.regex}"
                    }
                }
                
                // String Length Validation
                if (value is String) {
                    if (field.minLength != null && value.length < field.minLength) {
                        errors[field.key] = "Minimum length is ${field.minLength} characters"
                    }
                    if (field.maxLength != null && value.length > field.maxLength) {
                        errors[field.key] = "Maximum length is ${field.maxLength} characters"
                    }
                }

                // Numeric Range Validation
                if (field.type == FieldType.NUMBER) {
                    val numValue = value.toString().toDoubleOrNull()
                    if (numValue == null) {
                        errors[field.key] = "Must be a valid number"
                    } else {
                        if (field.min != null && numValue < field.min) errors[field.key] = "Minimum value is ${field.min}"
                        if (field.max != null && numValue > field.max) errors[field.key] = "Maximum value is ${field.max}"
                    }
                }
            }
        }
        validationErrors.value = errors
        return errors.isEmpty()
    }
}
