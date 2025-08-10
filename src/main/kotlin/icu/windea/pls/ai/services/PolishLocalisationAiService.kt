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
class PolishLocalisationAiService : ManipulateLocalisationAiService<PolishLocalisationAiRequest>() {
    private val logger = logger<PolishLocalisationAiService>()

    override fun manipulate(request: PolishLocalisationAiRequest): Flow<ParadoxLocalisationAiResult>? {
        //得到输入的本地化上下文，按特定数量进行分块，然后逐个发送请求（附带记忆）

        val chatModel = PlsChatModelManager.getStreamingChatModel() ?: return null
        val memory = getMemory()

        logger.info("${request.logPrefix} Polishing localisation...")
        val chunkSize = PlsAiFacade.getSettings().features.localisationChunkSize
        var chunkIndex = 0
        val startTime = System.currentTimeMillis()
        memory.add(getSystemMessage(request))
        return request.localisationContexts.asFlow().chunked(chunkSize).flatMapConcat { chunk ->
            logger.info("${request.logPrefix} Chunk #$chunkIndex: Sending request...")
            chunkIndex++
            memory.add(getUserMessage(request, chunk))
            chatModel.chatFlow {
                messages = memory.messages()
            }.toLineFlow().map { ParadoxLocalisationAiResult.fromLine(it) }.onCompletion { e ->
                val status = PlsAiManager.getChatFlowCompletionStatus(e)
                logger.info("${request.logPrefix} Chunk #$chunkIndex: ${status.text}")
            }.catchCompletion()
        }.onCompletion {
            val endTime = System.currentTimeMillis()
            val cost = endTime - startTime
            logger.info("${request.logPrefix} Polishing localisation finished in $cost ms")
        }
    }

    private fun getMemory(): ChatMemory {
        return MessageWindowChatMemory.withMaxMessages(Int.MAX_VALUE)
    }

    private fun getSystemMessage(request: PolishLocalisationAiRequest): SystemMessage {
        val text = PlsChatMessageManager.fromTemplate("polishLocalisation", request)
        logger.debug { "${request.logPrefix} System message: \n$text" }
        return SystemMessage.from(text)
    }

    private fun getUserMessage(request: PolishLocalisationAiRequest, chunk: List<ParadoxLocalisationContext>): UserMessage {
        val text = PlsChatMessageManager.fromLocalisationContexts(chunk)
        logger.debug { "${request.logPrefix} User message: \n$text" }
        return UserMessage.from(text)
    }
}
