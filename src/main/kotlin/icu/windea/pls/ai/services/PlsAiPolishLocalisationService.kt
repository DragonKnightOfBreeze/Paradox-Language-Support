package icu.windea.pls.ai.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import dev.langchain4j.data.message.*
import dev.langchain4j.kotlin.model.chat.*
import icu.windea.pls.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.flow.*
import java.awt.*

@Service
class PlsAiPolishLocalisationService : PlsAiManipulateLocalisationService() {
    fun polish(request: PlsAiPolishLocalisationRequest): Flow<ParadoxLocalisationResult>? {
        val chatModel = PlsChatModelManager.getStreamingChatModel() ?: return null

        logger.info("[AI REQUEST] Polish localisation...")
        return chatModel.chatFlow {
            messages += getMessages(request)
        }.toResultFlow()
    }

    private fun getMessages(request: PlsAiPolishLocalisationRequest): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        messages += getSystemMessage(request)
        messages += getUserMessage(request)
        return messages
    }

    private fun getSystemMessage(request: PlsAiPolishLocalisationRequest): SystemMessage {
        val text = PlsChatMessageManager.fromTemplate("polishLocalisation", request)
        logger.info("System message: \n$text")
        return SystemMessage.from(text)
    }

    private fun getUserMessage(request: PlsAiPolishLocalisationRequest): UserMessage {
        val text = PlsChatMessageManager.fromLocalisationContexts(request.localisationContexts)
        logger.info("User message: \n$text")
        return UserMessage.from(text)
    }

    fun createDescriptionPopup(project: Project, callback: (String) -> Unit): JBPopup {
        lateinit var popup: JBPopup
        val textField = TextFieldWithStoredHistory("PLS_AI_POLISH_LOCALISATION_DESCRIPTION_KEYS")
        val panel = panel {
            row {
                cell(textField).align(AlignX.FILL).focused().columns(COLUMNS_LARGE).smaller()
            }
            row {
                comment(PlsBundle.message("manipulation.localisation.popup.comment"), MAX_LINE_LENGTH_WORD_WRAP).smaller()
                button(PlsBundle.message("manipulation.localisation.popup.button.submit")) { popup.closeOk(null) }.smaller().align(AlignX.RIGHT)
            }
        }
        popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, textField)
            .setProject(project)
            .setRequestFocus(true)
            .setResizable(true)
            .setMovable(true)
            .setCancelOnClickOutside(false)
            .setCancelOnOtherWindowOpen(false)
            .setMinSize(Dimension(640, 120))
            .setTitle(PlsBundle.message("manipulation.localisation.popup.title.polish"))
            .setOkHandler { callback(textField.text.trim()) }
            .createPopup()
        textField.addActionListener { popup.closeOk(null) }
        return popup
    }
}
