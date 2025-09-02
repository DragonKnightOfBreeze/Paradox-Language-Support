@file:Suppress("unused")

package icu.windea.pls.ai.util

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import icu.windea.pls.ai.model.ChatModelType
import icu.windea.pls.ai.providers.ChatModelProvider
import icu.windea.pls.ai.providers.OpenAiChatModelProvider
import icu.windea.pls.core.EMPTY_OBJECT
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.cancelable

object PlsChatModelManager {
    private val chatModels = CacheBuilder().build<String, Any>().cancelable()
    private val streamingChatModels = CacheBuilder().build<String, Any>().cancelable()

    fun getTypeToUse(): ChatModelType {
        return ChatModelType.OPEN_AI
    }

    fun getProvider(type: ChatModelType): ChatModelProvider<*> {
        return when (type) {
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
