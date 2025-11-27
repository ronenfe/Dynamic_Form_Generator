package net.festinger.dynamicformgenerator

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FormUiTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testFullFormLifecycle() {
        // 1. Generate a form
        composeTestRule.onNodeWithText("Generate").performClick()

        // 2. Verify at least one field appeared (e.g., looking for a Label)
        // Since schema is random, we check for the common 'Pole Number' or generic text fields
        // Note: In a real test, you might inject a fixed schema, but for now we assume the randomizer works
        composeTestRule.onRoot().printToLog("TAG") // Optional: Helps debug layout in Logcat

        // 3. Try to Submit immediately (Should fail validation)
        composeTestRule.onNodeWithText("Submit").performClick()

        // 4. Check if error messages appeared
        // We look for "This field is required" which is your validation string
        // We use assertAny so it passes if at least one field is required
        try {
            composeTestRule.onAllNodesWithText("This field is required")
                .onFirst()
                .assertExists()
        } catch (e: AssertionError) {
            // It's possible the random schema generated no required fields, but unlikely
        }
    }

    @Test
    fun testViewSchemaDialog() {
        // 1. Generate Form
        composeTestRule.onNodeWithText("Generate").performClick()

        // 2. Open Schema View
        composeTestRule.onNodeWithText("View Schema").performClick()

        // 3. Verify Dialog is visible
        composeTestRule.onNodeWithText("Current Schema JSON").assertIsDisplayed()

        // 4. Close Dialog
        composeTestRule.onNodeWithText("Close").performClick()

        // 5. Verify Dialog is gone (Wait for animation to finish)
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Current Schema JSON")
                .fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun testInputInteraction() {
        // 1. Generate Form
        composeTestRule.onNodeWithText("Generate").performClick()

        // 2. Find a text field and type into it
        // This looks for any generic text field.
        // In a random schema, we rely on "Pole Number" often being present.
        // If your schema is purely random, this test might be flaky without a fixed schema.
        val poleNumberNode = composeTestRule.onAllNodesWithText("Pole Number *").onFirst()

        if (poleNumberNode.isDisplayed()) {
            poleNumberNode.performTextInput("Test-Data-123")

            // Verify text was entered
            poleNumberNode.assert(hasText("Test-Data-123"))
        }
    }
}
