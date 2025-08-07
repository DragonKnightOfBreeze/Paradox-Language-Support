package icu.windea.pls.ai.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import dev.langchain4j.data.message.*
import dev.langchain4j.kotlin.model.chat.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.flow.*

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
        return createDescriptionPopup(project, "PLS_AI_POLISH_LOCALISATION_DESCRIPTION_KEYS", callback)
    }
}
