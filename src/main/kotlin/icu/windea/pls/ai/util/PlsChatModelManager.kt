@file:Suppress("unused")

package icu.windea.pls.ai.util

import com.google.common.cache.*
import dev.langchain4j.model.chat.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.model.ChatModelType
import icu.windea.pls.core.*

object PlsChatModelManager {
    private val chatModels: Cache<String, Any> = CacheBuilder.newBuilder().build()
    private val streamingChatModels: Cache<String, Any> = CacheBuilder.newBuilder().build()

    fun getChatModelTypeToUse(): ChatModelType {
        return ChatModelType.OPEN_AI
    }

    fun isAvailable(type: ChatModelType = getChatModelTypeToUse()): Boolean {
        return checkSettings(type)
    }

    fun getChatModel(type: ChatModelType = getChatModelTypeToUse()): ChatModel? {
        return chatModels.get(type.name) {
            createChatModel(type) ?: EMPTY_OBJECT
        } as? ChatModel
    }

    fun getStreamingChatModel(type: ChatModelType = getChatModelTypeToUse()): StreamingChatModel? {
        return streamingChatModels.get(type.name) {
            createStreamingChatModel(type) ?: EMPTY_OBJECT
        } as? StreamingChatModel
    }

    fun invalidateChatModel(type: ChatModelType = getChatModelTypeToUse()) {
        chatModels.invalidate(type.name)
    }

    fun invalidateStreamingChatModel(type: ChatModelType = getChatModelTypeToUse()) {
        streamingChatModels.invalidate(type.name)
    }

    private fun checkSettings(type: ChatModelType): Boolean {
        return type.provider.isAvailable()
    }

    private fun createChatModel(type: ChatModelType): ChatModel? {
        return type.provider.createChatModel()
    }

    private fun createStreamingChatModel(type: ChatModelType): StreamingChatModel? {
        return type.provider.createStreamingChatModel()
    }
}
