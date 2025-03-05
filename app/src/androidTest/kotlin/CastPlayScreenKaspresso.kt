import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import io.github.kakaocup.compose.node.element.ComposeScreen
import io.github.kakaocup.compose.node.element.KNode

class CastPlayScreenKaspresso (semanticsProvider: SemanticsNodeInteractionsProvider): ComposeScreen<CastPlayScreenKaspresso>(
    semanticsProvider,
    viewBuilderAction = { hasTestTag("CastPlayScreen") }
) {
    val buttonSendLink:KNode = child { hasTestTag("sendLinkButton") }
    val statusText:KNode = child { hasTestTag("statusText") }
    val mediaButton:KNode = child { hasTestTag("mediaButton") }
}