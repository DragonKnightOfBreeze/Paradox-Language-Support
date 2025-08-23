@file:Suppress("unused")

package icu.windea.pls.ai.util

import com.google.common.cache.*
import dev.langchain4j.model.chat.*
import icu.windea.pls.ai.model.*
import icu.windea.pls.ai.providers.*
import icu.windea.pls.core.*

object PlsChatModelManager {
    private val chatModels: Cache<String, Any> = CacheBuilder.newBuilder().build()
    private val streamingChatModels: Cache<String, Any> = CacheBuilder.newBuilder().build()

    fun getTypeToUse(): ChatModelType {
        return ChatModelType.OPEN_AI
    }

    fun getProvider(type: ChatModelType): ChatModelProvider<*> {
        return when(type) {
            ChatModelType.OPEN_AI -> OpenAiChatModelProvider()
        }
    }

    fun isAvailable(type: ChatModelType = getTypeToUse()): Boolean {
        return getProvider(type).isAvailable()
    }

    fun getChatModel(type: ChatModelType = getTypeToUse()): ChatModel? {
        return chatModels.get(type.name) {
            getProvider(type).createChatModel() ?: EMPTY_OBJECT
        } as? ChatModel
    }

    fun getStreamingChatModel(type: ChatModelType = getTypeToUse()): StreamingChatModel? {
        return streamingChatModels.get(type.name) {
            getProvider(type).createStreamingChatModel() ?: EMPTY_OBJECT
        } as? StreamingChatModel
    }

    fun invalidateChatModel(type: ChatModelType = getTypeToUse()) {
        chatModels.invalidate(type.name)
    }

    fun invalidateStreamingChatModel(type: ChatModelType = getTypeToUse()) {
        streamingChatModels.invalidate(type.name)
    }
}
