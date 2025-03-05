import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.components.composesupport.config.withComposeSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import dev.realism.castplay.MainActivity
import io.github.kakaocup.compose.node.element.ComposeScreen.Companion.onComposeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CastPlayScreenTest : TestCase(kaspressoBuilder = Kaspresso.Builder.withComposeSupport()){
    @get:Rule val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testCastPlayScreen() = run {
        step("Ожидание загрузки экрана") {
            onComposeScreen<CastPlayScreenKaspresso>(composeTestRule) {
                statusText{
                    hasText("Ожидание нажатия кнопки")
                }
                buttonSendLink {
                    assertIsDisplayed()
                    performClick()
                }
                mediaButton{
                    isDialog()
                    assertIsDisplayed()
                }
            }
        }
    }
}