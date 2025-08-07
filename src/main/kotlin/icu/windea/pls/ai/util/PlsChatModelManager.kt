@file:Suppress("unused")

package icu.windea.pls.ai.util

import com.google.common.cache.*
import dev.langchain4j.model.chat.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.providers.*
import icu.windea.pls.core.*

object PlsChatModelManager {
    private val chatModels: Cache<String, Any> = CacheBuilder.newBuilder().build()
    private val streamingChatModels: Cache<String, Any> = CacheBuilder.newBuilder().build()

    fun getChatModelTypeToUse(): PlsChatModelType {
        return PlsChatModelType.OPEN_AI
    }

    fun isAvailable(type: PlsChatModelType = getChatModelTypeToUse()): Boolean {
        return checkSettings(type)
    }

    fun getChatModel(type: PlsChatModelType = getChatModelTypeToUse()): ChatModel? {
        return chatModels.get(type.name) {
            createChatModel(type) ?: EMPTY_OBJECT
        } as? ChatModel
    }

    fun getStreamingChatModel(type: PlsChatModelType = getChatModelTypeToUse()): StreamingChatModel? {
        return streamingChatModels.get(type.name) {
            createStreamingChatModel(type) ?: EMPTY_OBJECT
        } as? StreamingChatModel
    }

    fun invalidateChatModel(type: PlsChatModelType = getChatModelTypeToUse()) {
        chatModels.invalidate(type.name)
    }

    fun invalidateStreamingChatModel(type: PlsChatModelType = getChatModelTypeToUse()) {
        streamingChatModels.invalidate(type.name)
    }

    private fun checkSettings(type: PlsChatModelType): Boolean {
        return PlsChatModelProvider.get(type)?.isAvailable() ?: false
    }

    private fun createChatModel(type: PlsChatModelType): ChatModel? {
        return PlsChatModelProvider.get(type)?.createChatModel()
    }

    private fun createStreamingChatModel(type: PlsChatModelType): StreamingChatModel? {
        return PlsChatModelProvider.get(type)?.createStreamingChatModel()
    }
}
