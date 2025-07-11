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
import icu.windea.pls.ai.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.coroutines.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.awt.*
import java.lang.invoke.*

@Service
class PlsAiPolishLocalisationService : PlsAiManipulateLocalisationService() {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    fun polish(request: PlsAiPolishLocalisationsRequest): Flow<ParadoxLocalisationResult>? {
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

    private fun getSystemMessage(request: PlsAiPolishLocalisationsRequest): SystemMessage {
        val text = buildString {
            val contextLines = if (PlsAiManager.getSettings().withContext) {
                buildList {
                    request.filePath?.let { this += PlsAiBundle.message("systemMessage.context.0", it) }
                    request.fileName?.let { this += PlsAiBundle.message("systemMessage.context.1", it) }
                    request.modName?.let { this += PlsAiBundle.message("systemMessage.context.2", it) }
                }
            } else {
                emptyList()
            }
            if (contextLines.isEmpty()) {
                appendLine(PlsAiBundle.message("systemMessage.polishLocalisation.0", request.gameType))
            } else {
                appendLine(PlsAiBundle.message("systemMessage.polishLocalisation.1", request.gameType))
            }
            appendLine(PlsAiBundle.message("systemMessage.polishLocalisation.tip.1"))
            appendLine(PlsAiBundle.message("systemMessage.polishLocalisation.tip.2"))
            appendLine(PlsAiBundle.message("systemMessage.polishLocalisation.tip.3"))
            request.description?.orNull()?.let { appendLine(PlsAiBundle.message("systemMessage.polishLocalisation.tip.4", optimizeDescription(it))) }
            if (contextLines.isNotEmpty()) {
                appendLine(PlsAiBundle.message("systemMessage.context"))
                contextLines.forEach { appendLine(it) }
            }
        }.trim()
        logger.info("System message: \n$text")
        return SystemMessage.from(text)
    }

    private fun optimizeDescription(string: String): String {
        return string.trim().replace("\n", " ") //再次去除首尾空白，并且将换行符替换为空格
    }

    private fun getUserMessage(request: PlsAiPolishLocalisationsRequest): UserMessage {
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
