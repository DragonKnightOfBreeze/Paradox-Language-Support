package icu.windea.pls.ai.util

import com.google.common.cache.*
import dev.langchain4j.model.chat.*
import dev.langchain4j.model.openai.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.settings.*
import icu.windea.pls.core.*

object PlsChatModelManager {
    private val chatModels: Cache<String, Any> = CacheBuilder.newBuilder().build()
    private val streamingChatModels: Cache<String, Any> = CacheBuilder.newBuilder().build()

    fun getChatModel(type: PlsChatModelType = PlsAiManager.getChatModelTypeToUse()): ChatModel? {
        return chatModels.get(type.name) {
            createChatModel(type) ?: EMPTY_OBJECT
        } as? ChatModel
    }

    fun getStreamingChatModel(type: PlsChatModelType = PlsAiManager.getChatModelTypeToUse()): StreamingChatModel? {
        return streamingChatModels.get(type.name) {
            createStreamingChatModel(type) ?: EMPTY_OBJECT
        } as? StreamingChatModel
    }

    fun invalidateChatModel(type: PlsChatModelType = PlsAiManager.getChatModelTypeToUse()) {
        chatModels.invalidate(type.name)
    }

    fun invalidateStreamingChatModel(type: PlsChatModelType = PlsAiManager.getChatModelTypeToUse()) {
        streamingChatModels.invalidate(type.name)
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

    private fun createOpenAiChatModel(): OpenAiChatModel? {
        val settings = PlsAiManager.getSettings().openAI
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
        val settings = PlsAiManager.getSettings().openAI
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
