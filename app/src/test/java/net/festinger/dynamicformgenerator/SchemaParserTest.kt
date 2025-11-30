package net.festinger.dynamicformgenerator

import net.festinger.dynamicformgenerator.data.FieldType
import net.festinger.dynamicformgenerator.data.SchemaParser
import org.junit.Assert.*
import org.junit.Test

class SchemaParserTest {

    @Test
    fun `parseSchemas correctly merges Data and UI schemas`() {
        // 1. Define Mock JSON Strings
        val jsonSchema = """
            {
              "testField": {
                "type": "string",
                "label": "Test Label",
                "required": true
              }
            }
        """.trimIndent()

        val uiSchema = """
            {
              "testField": {
                "ui:placeholder": "Enter text here",
                "ui:widget": "textarea"
              }
            }
        """.trimIndent()

        // 2. Parse
        val result = SchemaParser.parseSchemas(jsonSchema, uiSchema)

        // 3. Assertions
        assertEquals(1, result.size)
        val field = result[0]

        assertEquals("testField", field.key)
        assertEquals(FieldType.STRING, field.type)
        assertEquals(true, field.required)

        // Check UI Options merge
        assertEquals("textarea", field.uiWidget)
        assertEquals("Enter text here", field.uiPlaceholder)
    }

    @Test
    fun `parseSchemas handles missing UI schema gracefully`() {
        val jsonSchema = """{ "age": { "type": "number", "label": "Age" } }"""
        val uiSchema = "{}" // Empty JSON object

        val result = SchemaParser.parseSchemas(jsonSchema, uiSchema)

        assertEquals(1, result.size)
        assertNull(result[0].uiWidget)
        assertNull(result[0].uiPlaceholder)
    }

    @Test
    fun `parseSchemas handles unknown types as STRING`() {
        val jsonSchema = """{ "weirdField": { "type": "alien_technology", "label": "Weird" } }"""
        val uiSchema = "{}"

        val result = SchemaParser.parseSchemas(jsonSchema, uiSchema)

        // Should default to STRING
        assertEquals(FieldType.STRING, result[0].type)
    }
}
