package icu.windea.pls.ai.util

import com.google.common.cache.*
import dev.langchain4j.model.chat.*
import dev.langchain4j.model.openai.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.settings.*
import icu.windea.pls.core.*

object PlsChatModelManager {
    private val chatModels: Cache<String, Any> = CacheBuilder.newBuilder().build()
    private val streamingChatModels: Cache<String, Any> = CacheBuilder.newBuilder().build()

    fun getChatModelTypeToUse(): PlsChatModelType {
        return PlsChatModelType.OPEN_AI
    }

    fun isValid(type: PlsChatModelType = getChatModelTypeToUse()): Boolean {
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
        return when (type) {
            PlsChatModelType.OPEN_AI -> checkOpenAiSettings()
        }
    }

    private fun createChatModel(type: PlsChatModelType): ChatModel? {
        return when (type) {
            PlsChatModelType.OPEN_AI -> createOpenAiChatModel()
        }
    }
    private fun createStreamingChatModel(type: PlsChatModelType): StreamingChatModel? {
        return when (type) {
            PlsChatModelType.OPEN_AI -> createOpenAiStreamingChatModel()
        }
    }

    private fun checkOpenAiSettings(): Boolean {
        // modelName 和 apiEndpoint 为空时使用默认值
        // apiKey 为空时直接报错
        return true
    }

    private fun createOpenAiChatModel(): OpenAiChatModel? {
        val settings = PlsAiFacade.getSettings().openAI
        val modelName = settings.modelName?.orNull() ?: PlsAiSettingsManager.getDefaultOpenAiModelName()
        val apiEndpoint = settings.apiEndpoint?.orNull() ?: PlsAiSettingsManager.getDefaultOpenAiApiEndpoint()
        val apiKey = settings.apiKey?.orNull() ?: return null
        return OpenAiChatModel.builder()
            .modelName(modelName)
            .baseUrl(apiEndpoint)
            .apiKey(apiKey)
            .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
            .strictJsonSchema(true)
            .build()
    }

    private fun createOpenAiStreamingChatModel(): OpenAiStreamingChatModel? {
        val settings = PlsAiFacade.getSettings().openAI
        val modelName = settings.modelName?.orNull() ?: return null
        val apiEndpoint = settings.apiEndpoint?.orNull() ?: return null
        val apiKey = settings.apiKey?.orNull() ?: return null
        return OpenAiStreamingChatModel.builder()
            .modelName(modelName)
            .baseUrl(apiEndpoint)
            .apiKey(apiKey)
            .build()
    }
}
