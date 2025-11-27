package net.festinger.dynamicformgenerator

import org.junit.Assert.*
import org.junit.Test

/**
 * Local unit tests for FormViewModel.
 * Verifies validation logic (Required, Regex, Min/Max) without needing an emulator. */
class FormViewModelTest {

    // A fixed schema for testing so we know exactly what fields exist
    private val testSchema = listOf(
        FormFieldSchema(
            key = "testRequired",
            label = "Required Field",
            type = FieldType.STRING,
            required = true
        ),
        FormFieldSchema(
            key = "testRegex",
            label = "Regex Field",
            type = FieldType.STRING,
            required = true,
            regex = "^[A-Z]{2}-\\d{4}$" // Example: NY-1234
        ),
        FormFieldSchema(
            key = "testNumber",
            label = "Number Field",
            type = FieldType.NUMBER,
            required = true,
            min = 10.0,
            max = 20.0
        )
    )

    @Test
    fun `generateNewForm loads schema and defaults`() {
        // Inject the test schema
        val viewModel = FormViewModel { testSchema }

        viewModel.generateNewForm()

        assertEquals(3, viewModel.schema.value.size)
        assertEquals("testRequired", viewModel.schema.value[0].key)
        assertTrue(viewModel.formData.isEmpty())
    }

    @Test
    fun `validation fails when required fields are empty`() {
        val viewModel = FormViewModel { testSchema }
        viewModel.generateNewForm()

        // Attempt submit with empty data
        viewModel.submitForm()

        val errors = viewModel.validationErrors.value
        assertTrue("Error missing for required field", errors.containsKey("testRequired"))
        assertTrue("Error missing for regex field", errors.containsKey("testRegex"))
        assertNull("Result should be null on error", viewModel.submissionResult.value)
    }

    @Test
    fun `validation fails for invalid regex format`() {
        val viewModel = FormViewModel { testSchema }
        viewModel.generateNewForm()

        // Valid data for other fields
        viewModel.onDataChanged("testRequired", "Valid")
        viewModel.onDataChanged("testNumber", "15")

        // Invalid Regex (lowercase instead of uppercase)
        viewModel.onDataChanged("testRegex", "ny-1234")

        viewModel.submitForm()

        val errors = viewModel.validationErrors.value
        assertTrue("Should have error for regex", errors.containsKey("testRegex"))
    }

    @Test
    fun `validation fails for number out of range`() {
        val viewModel = FormViewModel { testSchema }
        viewModel.generateNewForm()

        viewModel.onDataChanged("testRequired", "Valid")
        viewModel.onDataChanged("testRegex", "NY-1234")

        // Too Low (Minimum is 10.0)
        viewModel.onDataChanged("testNumber", "5")
        viewModel.submitForm()
        assertTrue(viewModel.validationErrors.value.containsKey("testNumber"))

        // Too High (Maximum is 20.0)
        viewModel.onDataChanged("testNumber", "25")
        viewModel.submitForm()
        assertTrue(viewModel.validationErrors.value.containsKey("testNumber"))
    }

    @Test
    fun `validation succeeds when all data is valid`() {
        val viewModel = FormViewModel { testSchema }
        viewModel.generateNewForm()

        // Fill all valid data
        viewModel.onDataChanged("testRequired", "Some Text")
        viewModel.onDataChanged("testRegex", "NY-9999")
        viewModel.onDataChanged("testNumber", "15.5")

        viewModel.submitForm()

        // Errors should be empty
        assertTrue(viewModel.validationErrors.value.isEmpty())

        // Result should be generated
        assertNotNull(viewModel.submissionResult.value)
    }
}
