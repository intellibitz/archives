package cc.thevar.blukit.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import cc.thevar.blukit.ui.theme.BlukitTheme
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testProfileScreenDisplaysNicknameField() {
        composeTestRule.setContent {
            BlukitTheme {
                ProfileScreen(
                    currentNickname = "vibe",
                    currentEmoji = "🎭",
                    isStealthMode = false,
                    onSaveNickname = {},
                    onSaveEmoji = {},
                    onToggleStealth = {},
                    onClearHistory = {},
                    onLogout = {}
                )
            }
        }

        // Label is now NAME YOUR VIBE
        composeTestRule.onNodeWithText("NAME YOUR VIBE").assertIsDisplayed()
        
        // Emoji section
        composeTestRule.onNodeWithText("PICK YOUR MOOD").assertIsDisplayed()
    }

    @Test
    fun testStillnessSection() {
        composeTestRule.setContent {
            BlukitTheme {
                ProfileScreen(
                    onSaveNickname = {},
                    onSaveEmoji = {},
                    onToggleStealth = {}
                )
            }
        }

        // Click to expand Stillness
        composeTestRule.onNodeWithText("▶ STILLNESS").performClick()
        
        // Buttons should appear
        composeTestRule.onNodeWithText("CLEAR WHISPERS").assertIsDisplayed()
        composeTestRule.onNodeWithText("DISSOLVE VIBE").assertIsDisplayed()
    }
}
