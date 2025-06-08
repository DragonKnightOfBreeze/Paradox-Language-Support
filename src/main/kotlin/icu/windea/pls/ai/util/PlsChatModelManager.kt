package icu.windea.pls.ai.util

import dev.langchain4j.model.chat.*
import dev.langchain4j.model.openai.*
import icu.windea.pls.*
import icu.windea.pls.core.*

object PlsChatModelManager {
    fun getOpenAiChatModel(jsonModel: Boolean = false): OpenAiChatModel? {
        val settings = PlsFacade.getAiSettings().openAI
        val modelName = settings.modelName?.orNull() ?: return null
        val apiEndpoint = settings.apiEndpoint?.orNull() ?: return null
        val apiKey = settings.apiKey?.orNull() ?: return null
        return OpenAiChatModel.builder()
            .modelName(modelName)
            .baseUrl(apiEndpoint)
            .apiKey(apiKey)
            .letIf(jsonModel) {
                it.supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                it.strictJsonSchema(true)
            }
            .build()
    }

    fun getOpenAiStreamChatModel(): OpenAiStreamingChatModel? {
        val settings = PlsFacade.getAiSettings().openAI
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
