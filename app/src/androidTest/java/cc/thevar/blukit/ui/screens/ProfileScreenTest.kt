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
    fun testProfileScreenDisplaysTitleAndNicknameField() {
        composeTestRule.setContent {
            BlukitTheme {
                ProfileScreen(
                    currentNickname = null,
                    currentEmoji = "👤",
                    isStealthMode = false,
                    onSaveNickname = {},
                    onSaveEmoji = {},
                    onToggleStealth = {},
                    onNavigateNext = {},
                    onClearHistory = {},
                    onLogout = {}
                )
            }
        }

        // Use a more generic matcher or check for the specific title text
        composeTestRule.onNodeWithText("Set Up Your Identity").assertExists()
        
        // Find by label text
        composeTestRule.onNodeWithText("Nickname").assertExists()
        
        // Find button by text and check enabled state
        composeTestRule.onNodeWithText("Start Exploring").assertIsNotEnabled()
    }

    @Test
    fun testStartExploringEnabledWhenNicknameEntered() {
        composeTestRule.setContent {
            BlukitTheme {
                ProfileScreen(
                    currentNickname = "",
                    currentEmoji = "👤",
                    isStealthMode = false,
                    onSaveNickname = {},
                    onSaveEmoji = {},
                    onToggleStealth = {},
                    onNavigateNext = {},
                    onClearHistory = {},
                    onLogout = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Nickname").performTextInput("BlukitUser")
        composeTestRule.onNodeWithText("Start Exploring").assertIsEnabled()
    }
}
