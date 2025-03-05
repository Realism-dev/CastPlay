package dev.realism.castplay

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()
    private val sendLinkButton = hasText("Отправить ссылку") and hasClickAction()

    @Test
    fun testMainActivity() {
        composeRule.onNode(sendLinkButton).assertIsDisplayed()
    }


}
