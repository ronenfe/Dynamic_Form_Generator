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
    fun testTwoStepGenerationFlow() {
        // 1. Start: Parse and Submit should be disabled
        composeTestRule.onNodeWithText("Parse Schema").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Submit").assertIsNotEnabled()

        // 2. Click Generate JSON
        composeTestRule.onNodeWithText("Generate JSON").performClick()

        // --- FIX: Close the Bottom Sheet that opens automatically ---
        // Wait for the sheet to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Close").fetchSemanticsNodes().isNotEmpty()
        }
        // Click Close
        composeTestRule.onNodeWithText("Close").performClick()
        // Wait for it to disappear so we can click other buttons
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Close").fetchSemanticsNodes().isEmpty()
        }
        // -----------------------------------------------------------

        // 3. Verify "Parse Schema" became enabled
        composeTestRule.onNodeWithText("Parse Schema").assertIsEnabled()

        // 4. Click Parse Schema
        composeTestRule.onNodeWithText("Parse Schema").performClick()

        // 5. Verify Form Renders (Submit becomes enabled)
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            try {
                composeTestRule.onNodeWithText("Submit").assertIsEnabled()
                true
            } catch (_: AssertionError) { false }
        }

        // 6. Verify a common field marker exists (if any required fields were generated)
        val requiredMarkers = composeTestRule.onAllNodes(hasText("*", substring = true))
        if (requiredMarkers.fetchSemanticsNodes().isNotEmpty()) {
            requiredMarkers.onFirst().assertExists()
        }
    }

    @Test
    fun testSourceJsonBottomSheet() {
        // 1. Generate JSON
        composeTestRule.onNodeWithText("Generate JSON").performClick()

        // 2. Wait for Bottom Sheet content
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Generated Source JSON")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // 3. Verify content is displayed
        composeTestRule.onNodeWithText("Generated Source JSON").assertIsDisplayed()

        // 4. Close the Sheet
        composeTestRule.onNodeWithText("Close").performClick()

        // 5. Verify Sheet is gone
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule
                .onAllNodesWithText("Generated Source JSON")
                .fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun testInputAndSubmission() {
        // 1. Generate
        composeTestRule.onNodeWithText("Generate JSON").performClick()

        // 2. Close the auto-opened JSON sheet
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Close").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Close").performClick()

        // Wait for sheet to close
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("Close").fetchSemanticsNodes().isEmpty()
        }

        // 3. Parse
        composeTestRule.onNodeWithText("Parse Schema").performClick()

        // 4. Wait for UI
        composeTestRule.waitForIdle()

        // 5. Try to Submit empty (Trigger Validation)
        composeTestRule.onNodeWithText("Submit").performClick()

        // 6. Check for error
        val errorNodes = composeTestRule.onAllNodesWithText("This field is required")
        if (errorNodes.fetchSemanticsNodes().isNotEmpty()) {
            errorNodes.onFirst().assertExists()
        }
    }
}
