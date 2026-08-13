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

        // Title is now MASK
        composeTestRule.onNodeWithText("MASK").assertExists()
        
        // Label is now PROTOCOL DESIGNATION
        composeTestRule.onNodeWithText("PROTOCOL DESIGNATION").assertExists()
        
        // Button is now DON THE MASK
        composeTestRule.onNodeWithText("DON THE MASK").assertIsNotEnabled()
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

        composeTestRule.onNodeWithText("PROTOCOL DESIGNATION").performTextInput("BlukitUser")
        composeTestRule.onNodeWithText("DON THE MASK").assertIsEnabled()
    }
}
