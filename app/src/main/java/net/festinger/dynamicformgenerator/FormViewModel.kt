package net.festinger.dynamicformgenerator

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder

class FormViewModel(
    private val schemaProvider: () -> List<FormFieldSchema> = { SchemaGenerator.generateRandomSchema() }
) : ViewModel() {

    var schema = mutableStateOf<List<FormFieldSchema>>(emptyList())
        private set

    val formData = mutableStateMapOf<String, Any?>()
    var validationErrors = mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var submissionResult = mutableStateOf<String?>(null)
    var showSchemaDialog = mutableStateOf(false)

    fun generateNewForm() {
        // CHANGE 2: Use the provider instead of hardcoding SchemaGenerator
        val newSchema = schemaProvider()
        schema.value = newSchema

        formData.clear()
        validationErrors.value = emptyMap()
        submissionResult.value = null

        newSchema.forEach { field ->
            if (field.type == FieldType.BOOLEAN) {
                formData[field.key] = false
            }
        }
    }

    // ... (rest of the file remains exactly the same: onDataChanged, submitForm, validateForm) ...
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
                if (field.regex != null && value is String) {
                    if (!value.matches(Regex(field.regex))) {
                        errors[field.key] = "Invalid format. Expected pattern: ${field.regex}"
                    }
                }
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
