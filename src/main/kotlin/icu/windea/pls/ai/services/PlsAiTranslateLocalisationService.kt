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
class PlsAiTranslateLocalisationService : PlsAiManipulateLocalisationService() {
    fun translate(request: PlsAiTranslateLocalisationRequest): Flow<ParadoxLocalisationResult>? {
        val chatModel = PlsChatModelManager.getStreamingChatModel() ?: return null

        logger.info("[AI REQUEST] Translate localisation...")
        return chatModel.chatFlow {
            messages += getMessages(request)
        }.toResultFlow()
    }

    private fun getMessages(request: PlsAiTranslateLocalisationRequest): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        messages += getSystemMessage(request)
        messages += getUserMessage(request)
        return messages
    }

    private fun getSystemMessage(request: PlsAiTranslateLocalisationRequest): SystemMessage {
        val text = PlsChatMessageManager.fromTemplate("translateLocalisation", request)
        logger.info("System message: \n$text")
        return SystemMessage.from(text)
    }

    private fun getUserMessage(request: PlsAiTranslateLocalisationRequest): UserMessage {
        val text = PlsChatMessageManager.fromLocalisationContexts(request.localisationContexts)
        logger.info("User message: \n$text")
        return UserMessage.from(text)
    }

    fun createDescriptionPopup(project: Project, callback: (String) -> Unit): JBPopup {
        return createDescriptionPopup(project, "PLS_AI_TRANSLATE_LOCALISATION_DESCRIPTION_KEYS", callback)
    }
}
