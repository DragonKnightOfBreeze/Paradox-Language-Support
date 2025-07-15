package icu.windea.pls.ai.services

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import dev.langchain4j.data.message.*
import dev.langchain4j.kotlin.model.chat.*
import icu.windea.pls.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.core.coroutines.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.awt.*
import java.lang.invoke.*

@Service
class PlsAiPolishLocalisationService : PlsAiManipulateLocalisationService() {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    fun polish(request: PlsAiPolishLocalisationRequest): Flow<ParadoxLocalisationResult>? {
        val chatModel = PlsChatModelManager.getStreamingChatModel() ?: return null

        logger.info("[AI REQUEST] Polish localisation...")
        return chatModel.chatFlow f2@{
            messages += getSystemMessage(request)
            messages += getUserMessage(request)
        }.toLineFlow({
            when (it) {
                is StreamingChatModelReply.PartialResponse -> it.partialResponse
                is StreamingChatModelReply.CompleteResponse -> ""
                is StreamingChatModelReply.Error -> throw it.cause
            }
        }, {
            ParadoxLocalisationResult.fromLine(it)
        }).onCompletion { e ->
            when {
                e is CancellationException -> logger.warn("[AI RESPONSE] Cancelled.")
                e != null -> logger.warn("[AI RESPONSE] Failed.", e)
                else -> logger.info("[AI RESPONSE] Done.")
            }
        }
    }

    private fun getSystemMessage(request: PlsAiPolishLocalisationRequest): SystemMessage {
        val text = PlsPromptManager.fromTemplate("polishLocalisation", request)
        logger.info("System message: \n$text")
        return SystemMessage.from(text)
    }

    private fun getUserMessage(request: PlsAiPolishLocalisationRequest): UserMessage {
        val text = request.text
        logger.info("User message: \n$text")
        return UserMessage.from(text)
    }

    fun createDescriptionPopup(project: Project, callback: (String) -> Unit): JBPopup {
        val textField = TextFieldWithStoredHistory("PLS_AI_POLISH_LOCALISATION_DESCRIPTION_KEYS")
        val panel = panel {
            row {
                cell(textField).align(AlignX.FILL).columns(COLUMNS_LARGE).focused()
                    .comment(PlsBundle.message("manipulation.localisation.polish.popup.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            }
        }
        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, textField)
            .setRequestFocus(true)
            .setResizable(true)
            .setMovable(true)
            .setCancelOnClickOutside(false)
            .setCancelOnOtherWindowOpen(false)
            .setMinSize(Dimension(640, 120))
            .setTitle(PlsBundle.message("manipulation.localisation.polish.popup.title"))
            .createPopup()
        textField.addActionListener {
            popup.closeOk(null)
            callback(textField.text.trim())
        }
        return popup
    }
}
