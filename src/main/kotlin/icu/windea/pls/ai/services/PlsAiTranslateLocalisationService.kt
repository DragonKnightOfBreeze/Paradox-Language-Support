package icu.windea.pls.ai.services

import com.intellij.openapi.diagnostic.*
import dev.langchain4j.data.message.*
import dev.langchain4j.kotlin.model.chat.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import kotlinx.coroutines.flow.*
import java.lang.invoke.*

object PlsAiTranslateLocalisationService : PlsAiService {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())

    fun supports(): Boolean {
        return PlsAiManager.isEnabled() && PlsChatModelManager.getStreamingChatModel() != null
    }

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
        })
    }

    private fun getSystemMessage(request: PlsAiTranslateLocalisationsRequest): SystemMessage {
        val text = buildString {
            val contextLines = if (PlsAiManager.getSettings().withContext) {
                buildList {
                    request.filePath?.let { this += PlsAiDocBundle.message("systemMessage.translateLocalisation.context.0", it) }
                    request.fileName?.let { this += PlsAiDocBundle.message("systemMessage.translateLocalisation.context.1", it) }
                    request.modName?.let { this += PlsAiDocBundle.message("systemMessage.translateLocalisation.context.2", it) }
                }
            } else {
                emptyList()
            }
            if (contextLines.isEmpty()) {
                append(PlsAiDocBundle.message("systemMessage.translateLocalisation.0", request.gameType, request.targetLocale))
                appendLine(PlsAiDocBundle.message("systemMessage.translateLocalisation.tip"))
            } else {
                appendLine(PlsAiDocBundle.message("systemMessage.translateLocalisation.1", request.gameType, request.targetLocale))
                appendLine(PlsAiDocBundle.message("systemMessage.translateLocalisation.tip"))
                appendLine(PlsAiDocBundle.message("systemMessage.translateLocalisation.context"))
                contextLines.forEach { appendLine(it) }
            }
        }
        logger.info("System message: \n$text")
        return SystemMessage.from(text)
    }

    private fun getUserMessage(request: PlsAiTranslateLocalisationsRequest): UserMessage {
        val text = request.text
        logger.info("User message: \n$text")
        return UserMessage.from(text)
    }
}
