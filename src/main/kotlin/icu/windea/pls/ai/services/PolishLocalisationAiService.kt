@file:Suppress("OPT_IN_USAGE", "AssignedValueIsNeverRead")

package icu.windea.pls.ai.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.request.ChatRequest
import icu.windea.pls.ai.model.chatFlow
import icu.windea.pls.ai.model.onCompletionResult
import icu.windea.pls.ai.model.requests.PolishLocalisationAiRequest
import icu.windea.pls.ai.model.results.LocalisationAiResult
import icu.windea.pls.ai.model.toLineFlow
import icu.windea.pls.ai.providers.ChatModelManager
import icu.windea.pls.ai.util.PlsChatMessageManager
import icu.windea.pls.ai.util.PlsPrompts
import icu.windea.pls.core.coroutines.chunked
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion

@Service
class PolishLocalisationAiService : ManipulateLocalisationAiService<PolishLocalisationAiRequest>() {
    private val logger = logger<PolishLocalisationAiService>()

    override fun manipulate(request: PolishLocalisationAiRequest): Flow<LocalisationAiResult>? {
        //得到输入的本地化上下文，按特定数量进行分块，然后逐个发送请求（附带记忆）

        val chatModel = ChatModelManager.getStreamingChatModel() ?: return null
        val memory = getMemory()

        logger.info("${request.logPrefix} Polishing localisation...")
        val chunkSize = getChunkSize()
        var chunkIndex = 0
        val startTime = System.currentTimeMillis()
        memory.add(getSystemMessage(request))
        return request.localisationContexts.asFlow().chunked(chunkSize)
            .flatMapConcat { chunk ->
                logger.info("${request.logPrefix} Chunk #$chunkIndex: Sending request...")
                chunkIndex++
                memory.add(getUserMessage(request, chunk))
                val chatRequest = ChatRequest.builder().messages(memory.messages()).build()
                chatModel.chatFlow(chatRequest)
                    .onCompletionResult { result ->
                        logger.info("${request.logPrefix} Chunk #$chunkIndex: ${result.statusText()}")
                        logger.info("${request.logPrefix} Chunk #$chunkIndex: ${result.metadataText()}")
                        if (result.thinking.isNotEmpty()) logger.debug { "${request.logPrefix} Chunk #$chunkIndex: <THINKING>\n${result.thinking}" }
                        if (result.text.isNotEmpty()) logger.debug { "${request.logPrefix} Chunk #$chunkIndex:\n${result.text}" }
                    }
                    .toLineFlow()
                    .map { LocalisationAiResult.fromLine(it) }
            }
            .onCompletion {
                val endTime = System.currentTimeMillis()
                val cost = endTime - startTime
                logger.info("${request.logPrefix} Polishing localisation finished in $cost ms")
            }
    }

    private fun getSystemMessage(request: PolishLocalisationAiRequest): SystemMessage {
        val text = PlsChatMessageManager.fromTemplate(PlsPrompts.PolishLocalisation, request)
        logger.debug { "${request.logPrefix} System message: \n$text" }
        return SystemMessage.from(text)
    }

    private fun getUserMessage(request: PolishLocalisationAiRequest, chunk: List<ParadoxLocalisationContext>): UserMessage {
        val text = PlsChatMessageManager.fromLocalisationContexts(chunk)
        logger.debug { "${request.logPrefix} User message: \n$text" }
        return UserMessage.from(text)
    }
}
