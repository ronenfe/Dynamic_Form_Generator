package net.festinger.dynamicformgenerator

import net.festinger.dynamicformgenerator.data.FieldType
import net.festinger.dynamicformgenerator.viewmodel.FormViewModel
import org.junit.Assert.*
import org.junit.Test

class FormViewModelTest {

    @Test
    fun `loadRandomJson generates strings and updates rawJson state`() {
        val viewModel = FormViewModel()

        // 1. Call the new generation method
        viewModel.loadRandomJson()

        // 2. Verify that the raw JSON string is populated
        assertTrue("Raw JSON should not be empty", viewModel.rawSchemaJson.value.isNotEmpty())
        assertTrue("Should contain 'Data Schema'", viewModel.rawSchemaJson.value.contains("Data Schema"))

        // 3. Verify that the schema list is CLEARED (waiting for parse)
        assertTrue("Schema list should be empty before parsing", viewModel.schema.value.isEmpty())

        // 4. Verify dialog trigger
        assertTrue("Dialog/Sheet should be visible", viewModel.showSchemaDialog.value)
    }

    @Test
    fun `parseJson populates the schema list`() {
        val viewModel = FormViewModel()

        // 1. Load data first
        viewModel.loadRandomJson()

        // 2. Parse it
        viewModel.parseJson()

        // 3. Verify we now have objects
        assertTrue("Schema list should contain fields after parsing", viewModel.schema.value.isNotEmpty())

        // 4. Verify defaults are set (e.g. Booleans in formData)
        // We check if any booleans exist in the random schema, if so, they must be in formData
        val booleanFields = viewModel.schema.value.filter { it.type == FieldType.BOOLEAN }
        booleanFields.forEach { field ->
            val value = viewModel.formData[field.key]
            assertTrue("Field ${field.key} should be a Boolean", value is Boolean)
        }
    }

    @Test
    fun `validation logic works on parsed objects`() {
        val viewModel = FormViewModel()
        viewModel.loadRandomJson()
        viewModel.parseJson()

        // Find a required field if one exists
        val requiredField = viewModel.schema.value.find { it.required }

        if (requiredField != null) {
            // 1. Explicitly clear the data for this key to ensure it's null
            // (Sometimes defaults might set it to "")
            viewModel.formData.remove(requiredField.key)

            // 2. Submit empty
            viewModel.submitForm()

            // Check error
            assertTrue("Should have error for required field ${requiredField.key}",
                viewModel.validationErrors.value.containsKey(requiredField.key))

            // Fix error
            viewModel.onDataChanged(requiredField.key, "Some Value")

            // Check error removed
            assertFalse(viewModel.validationErrors.value.containsKey(requiredField.key))
        }
    }
}
