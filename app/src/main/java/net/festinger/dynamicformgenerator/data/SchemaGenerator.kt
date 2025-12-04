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
            "maintenanceDate", "technicianNotes", "sitePhoto", "gpsCoordinates",
            "shiftTime", "isPowerCut", "partsReplaced",
            "emailContact", "urgencyLevel",
            "supervisorPhone", "riskScore", "safetyProtocols",
            "referenceLink", "accessCode"
        )

        // Randomly pick 5 to 10 fields to make each form look different
        rawFields.shuffled().take((5..10).random()).forEach { key ->

            val fieldDef = mutableMapOf<String, Any?>()
            fieldDef["label"] = formatLabel(key)

            // Configure field properties based on the key
            when (key) {
                "poleNumber" -> {
                    fieldDef["type"] = "string"
                    fieldDef["required"] = true
                    fieldDef["regex"] = "^[A-Z]{2}-\\d{4}$"

                    uiMap[key] = mapOf(
                        "ui:placeholder" to "Format: NY-1234",
                        "ui:autofocus" to true,
                        "ui:help" to "Enter the ID stamped on the pole plate",
                        "ui:icon" to "tag"
                    )
                }
                "fixtureHeight" -> {
                    fieldDef["type"] = "number"
                    fieldDef["min"] = 3.0
                    fieldDef["max"] = 30.0
                    fieldDef["default"] = 5.0

                    uiMap[key] = mapOf(
                        "ui:placeholder" to "Height in meters",
                        "ui:help" to "Measure from ground to base",
                        "ui:step" to 0.5,
                        "ui:icon" to "height"
                    )
                }
                "bulbType" -> {
                    fieldDef["type"] = "dropdown"
                    fieldDef["options"] = listOf("LED", "HPS", "Metal Halide")
                    fieldDef["required"] = true
                    fieldDef["default"] = "LED"
                    
                    uiMap[key] = mapOf(
                        "ui:placeholder" to "Select...",
                        "ui:help" to "Check housing for type",
                        "ui:widget" to "spinner"
                    )
                }
                "technicianNotes" -> {
                    fieldDef["type"] = "string"
                    fieldDef["minLength"] = 10
                    fieldDef["maxLength"] = 500
                    
                    uiMap[key] = mapOf(
                        "ui:widget" to "textarea",
                        "ui:placeholder" to "Enter detailed report...",
                        "ui:help" to "Describe damage or repairs",
                        "ui:lines" to 4,
                        "ui:maxLines" to 8
                    )
                }
                "maintenanceDate" -> {
                    fieldDef["type"] = "date"
                    uiMap[key] = mapOf(
                        "ui:widget" to "datepicker",
                        "ui:help" to "Date of service completion",
                        "ui:format" to "yyyy-MM-dd"
                    )
                }
                "shiftTime" -> {
                    fieldDef["type"] = "time"
                    uiMap[key] = mapOf(
                        "ui:widget" to "timepicker",
                        "ui:help" to "Time work started"
                    )
                }
                "isPowerCut" -> {
                    fieldDef["type"] = "boolean"
                    fieldDef["default"] = false
                    
                    uiMap[key] = mapOf(
                        "ui:widget" to "checkbox",
                        "ui:help" to "Was power disconnected before work?"
                    )
                }
                "partsReplaced" -> {
                    fieldDef["type"] = "multiselect"
                    fieldDef["options"] = listOf("Fuse", "Photocell", "Wiring", "Bracket", "Lens")
                    uiMap[key] = mapOf(
                        "ui:widget" to "dialog",
                        "ui:help" to "Select all parts used"
                    )
                }
                "sitePhoto" -> {
                    fieldDef["type"] = "image"
                    uiMap[key] = mapOf(
                        "ui:widget" to "camera",
                        "ui:help" to "Capture photo of repair",
                        "ui:buttonLabel" to "Take Photo"
                    )
                }
                "gpsCoordinates" -> {
                    fieldDef["type"] = "gps"
                    uiMap[key] = mapOf(
                        "ui:widget" to "gps_button",
                        "ui:help" to "Stand at base of pole",
                        "ui:buttonLabel" to "Get Location",
                        "ui:icon" to "my_location"
                    )
                }
                "emailContact" -> {
                    fieldDef["type"] = "string"
                    fieldDef["regex"] = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                    uiMap[key] = mapOf(
                        "ui:placeholder" to "tech@example.com",
                        "ui:icon" to "email",
                        "ui:inputType" to "email",
                        "ui:help" to "Supervisor email for report"
                    )
                }
                "urgencyLevel" -> {
                    fieldDef["type"] = "dropdown"
                    fieldDef["options"] = listOf("Routine", "Urgent", "Emergency")
                    fieldDef["default"] = "Routine"
                    uiMap[key] = mapOf(
                        "ui:help" to "Impact on public safety",
                        "ui:widget" to "radio"
                    )
                }
                "supervisorPhone" -> {
                    fieldDef["type"] = "string"
                    fieldDef["regex"] = "^\\+?[0-9]{10,15}$"
                    uiMap[key] = mapOf(
                        "ui:placeholder" to "+15551234567",
                        "ui:icon" to "phone",
                        "ui:inputType" to "phone",
                        "ui:help" to "Emergency contact number"
                    )
                }
                "riskScore" -> {
                    fieldDef["type"] = "number"
                    fieldDef["min"] = 1.0
                    fieldDef["max"] = 10.0
                    fieldDef["default"] = 1.0
                    uiMap[key] = mapOf(
                        "ui:widget" to "slider",
                        "ui:step" to 1.0,
                        "ui:help" to "1 = Safe, 10 = Critical Danger",
                        "ui:icon" to "warning"
                    )
                }
                "safetyProtocols" -> {
                    fieldDef["type"] = "boolean"
                    fieldDef["default"] = true
                    uiMap[key] = mapOf(
                        "ui:widget" to "switch",
                        "ui:help" to "Have all safety checks been performed?"
                    )
                }
                "referenceLink" -> {
                    fieldDef["type"] = "string"
                    fieldDef["regex"] = "^https?://.+$"
                    uiMap[key] = mapOf(
                        "ui:placeholder" to "https://docs.example.com",
                        "ui:inputType" to "textUri",
                        "ui:icon" to "link",
                        "ui:help" to "Link to technical manual"
                    )
                }
                "accessCode" -> {
                    fieldDef["type"] = "string"
                    fieldDef["minLength"] = 4
                    fieldDef["maxLength"] = 6
                    uiMap[key] = mapOf(
                        "ui:placeholder" to "****",
                        "ui:inputType" to "numberPassword",
                        "ui:icon" to "lock",
                        "ui:help" to "Site security PIN"
                    )
                }
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
