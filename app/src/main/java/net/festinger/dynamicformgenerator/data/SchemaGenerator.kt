package net.festinger.dynamicformgenerator.data

import com.google.gson.GsonBuilder
import java.util.Locale

object SchemaGenerator {

    /*** Generates a random pair of JSON strings:
     * 1. Data Schema JSON (Validation, Types)
     * 2. UI Schema JSON (Widgets, Placeholders)
     */
    fun generateRandomJsonStrings(): Pair<String, String> {

        val dataMap = mutableMapOf<String, Map<String, Any?>>()
        val uiMap = mutableMapOf<String, Map<String, Any>>()

        // A pool of potential fields relevant to your technician use case
        val rawFields = listOf(
            "poleNumber", "fixtureHeight", "bulbType",
            "maintenanceDate", "technicianNotes", "sitePhoto", "gpsCoordinates"
        )

        // Randomly pick 3 to 6 fields to make each form look different
        rawFields.shuffled().take((3..6).random()).forEach { key ->

            val fieldDef = mutableMapOf<String, Any?>()
            fieldDef["label"] = formatLabel(key)

            // Configure field properties based on the key
            when (key) {
                "poleNumber" -> {
                    fieldDef["type"] = "string"
                    fieldDef["required"] = true
                    fieldDef["regex"] = "^[A-Z]{2}-\\d{4}$"

                    // UI Schema: specific placeholder and autofocus
                    uiMap[key] = mapOf(
                        "ui:placeholder" to "Format: NY-1234",
                        "ui:autofocus" to true
                    )
                }
                "fixtureHeight" -> {
                    fieldDef["type"] = "number"
                    fieldDef["min"] = 3.0
                    fieldDef["max"] = 30.0

                    uiMap[key] = mapOf("ui:placeholder" to "Height in meters")
                }
                "bulbType" -> {
                    fieldDef["type"] = "dropdown"
                    fieldDef["options"] = listOf("LED", "HPS", "Metal Halide")
                    fieldDef["required"] = true
                }
                "technicianNotes" -> {
                    fieldDef["type"] = "string"
                    // UI Schema: Use a textarea widget for notes
                    uiMap[key] = mapOf(
                        "ui:widget" to "textarea",
                        "ui:placeholder" to "Enter detailed report..."
                    )
                }
                "maintenanceDate" -> fieldDef["type"] = "date"
                "sitePhoto" -> fieldDef["type"] = "image"
                "gpsCoordinates" -> fieldDef["type"] = "gps"
                else -> fieldDef["type"] = "string"
            }

            dataMap[key] = fieldDef
        }

        // Convert the maps to JSON Strings using GSON
        val gson = GsonBuilder().setPrettyPrinting().create()
        return Pair(gson.toJson(dataMap), gson.toJson(uiMap))
    }

    // Helper to make "poleNumber" look like "Pole Number"
    private fun formatLabel(key: String): String {
        return key.replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
}
