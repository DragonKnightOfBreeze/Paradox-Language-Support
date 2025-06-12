package icu.windea.pls.ai.services

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.MAX_LINE_LENGTH_WORD_WRAP
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import dev.langchain4j.data.message.*
import dev.langchain4j.kotlin.model.chat.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.awt.Dimension
import java.lang.invoke.*

@Service
class PlsAiTranslateLocalisationService : PlsAiManipulateLocalisationService() {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    fun translate(request: PlsAiTranslateLocalisationsRequest): Flow<ParadoxLocalisationData>? {
        val chatModel = PlsChatModelManager.getStreamingChatModel() ?: return null

        logger.info("[AI REQUEST] Translate localisation...")
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
            ParadoxLocalisationData.fromLine(it)
        }).onCompletion { e ->
            when {
                e is CancellationException -> logger.warn("[AI RESPONSE] Cancelled.")
                e != null -> logger.warn("[AI RESPONSE] Failed.", e)
                else -> logger.info("[AI RESPONSE] Done.")
            }
        }
    }

    private fun getSystemMessage(request: PlsAiTranslateLocalisationsRequest): SystemMessage {
        val text = buildString {
            val contextLines = if (PlsAiManager.getSettings().withContext) {
                buildList {
                    request.filePath?.let { this += PlsAiDocBundle.message("systemMessage.context.0", it) }
                    request.fileName?.let { this += PlsAiDocBundle.message("systemMessage.context.1", it) }
                    request.modName?.let { this += PlsAiDocBundle.message("systemMessage.context.2", it) }
                }
            } else {
                emptyList()
            }
            if (contextLines.isEmpty()) {
                appendLine(PlsAiDocBundle.message("systemMessage.translateLocalisation.0", request.gameType, request.targetLocale))
            } else {
                appendLine(PlsAiDocBundle.message("systemMessage.translateLocalisation.1", request.gameType, request.targetLocale))
            }
            appendLine(PlsAiDocBundle.message("systemMessage.translateLocalisation.tip.1"))
            appendLine(PlsAiDocBundle.message("systemMessage.translateLocalisation.tip.2"))
            appendLine(PlsAiDocBundle.message("systemMessage.translateLocalisation.tip.3", request.targetLocale))
            request.description?.orNull()?.let { appendLine(PlsAiDocBundle.message("systemMessage.translateLocalisation.tip.4", optimizeDescription(it))) }
            if (contextLines.isNotEmpty()) {
                appendLine(PlsAiDocBundle.message("systemMessage.context"))
                contextLines.forEach { appendLine(it) }
            }
        }.trim()
        logger.info("System message: \n$text")
        return SystemMessage.from(text)
    }

    private fun optimizeDescription(string: String): String {
        return string.trim().replace("\n", " ") //再次去除首尾空白，并且将换行符替换为空格
    }

    private fun getUserMessage(request: PlsAiTranslateLocalisationsRequest): UserMessage {
        val text = request.text
        logger.info("User message: \n$text")
        return UserMessage.from(text)
    }

    fun createDescriptionPopup(project: Project, editor: Editor?, file: PsiFile?, callback: (String) -> Unit): JBPopup {
        val textField = JBTextField()
        val panel = panel {
            row {
                cell(textField).align(AlignX.FILL).columns(COLUMNS_LARGE).focused()
                    .comment(PlsAiBundle.message("intention.localisation.translate.popup.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            }
        }
        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, textField)
            .setRequestFocus(true)
            .setResizable(true)
            .setMovable(true)
            .setCancelOnClickOutside(false)
            .setCancelOnOtherWindowOpen(false)
            .setMinSize(Dimension(640, 60))
            .setTitle(PlsAiBundle.message("intention.localisation.translate.popup.title"))
            .createPopup()
        textField.addActionListener {
            popup.closeOk(null)
            callback(textField.text.trim())
        }
        return popup
    }
}
