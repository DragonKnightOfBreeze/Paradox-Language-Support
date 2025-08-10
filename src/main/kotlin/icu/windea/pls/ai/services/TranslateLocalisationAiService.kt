@file:Suppress("OPT_IN_USAGE", "AssignedValueIsNeverRead")

package icu.windea.pls.ai.services

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import dev.langchain4j.data.message.*
import dev.langchain4j.kotlin.model.chat.*
import dev.langchain4j.memory.*
import dev.langchain4j.memory.chat.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.ai.util.manipulators.*
import icu.windea.pls.core.coroutines.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.flow.*

@Service
class TranslateLocalisationAiService : ManipulateLocalisationAiService<TranslateLocalisationAiRequest>() {
    private val logger = logger<TranslateLocalisationAiService>()

    override fun manipulate(request: TranslateLocalisationAiRequest): Flow<ParadoxLocalisationAiResult>? {
        //得到输入的本地化上下文，按特定数量进行分块，然后逐个发送请求（附带记忆）

        val chatModel = PlsChatModelManager.getStreamingChatModel() ?: return null
        val memory = getMemory()

        logger.info("[AI REQUEST] Translating localisation...")
        val chunkSize = PlsAiFacade.getSettings().features.localisationChunkSize
        var chunkIndex = 0
        val startTime = System.currentTimeMillis()
        memory.add(getSystemMessage(request))
        return request.localisationContexts.asFlow().chunked(chunkSize).flatMapConcat { chunk ->
            val requestId = "#$chunkIndex"
            logger.info("[AI REQUEST] Request $requestId: Sending...")
            chunkIndex++
            memory.add(getUserMessage(chunk))
            chatModel.chatFlow {
                messages = memory.messages()
            }.toLineFlow().map { ParadoxLocalisationAiResult.fromLine(it) }.onCompletion { e ->
                val status = PlsAiManager.getChatFlowCompletionStatus(e)
                logger.info("[AI RESPONSE] Request $requestId: ${status.text}")
            }.catchCompletion()
        }.onCompletion {
            val endTime = System.currentTimeMillis()
            val cost = endTime - startTime
            logger.info("[AI REQUEST] Translating localisation finished in $cost ms")
        }
    }

    private fun getMemory(): ChatMemory {
        return MessageWindowChatMemory.withMaxMessages(Int.MAX_VALUE)
    }

    private fun getSystemMessage(request: TranslateLocalisationAiRequest): SystemMessage {
        val text = PlsChatMessageManager.fromTemplate("translateLocalisation", request)
        logger.info("System message: \n$text")
        return SystemMessage.from(text)
    }

    private fun getUserMessage(chunk: List<ParadoxLocalisationContext>): UserMessage {
        val text = PlsChatMessageManager.fromLocalisationContexts(chunk)
        logger.info("User message: \n$text")
        return UserMessage.from(text)
    }
}
