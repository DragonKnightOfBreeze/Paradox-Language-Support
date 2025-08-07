package icu.windea.pls.ai.providers

import dev.langchain4j.model.chat.Capability
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.PlsChatModelType
import icu.windea.pls.ai.settings.PlsAiSettingsManager
import icu.windea.pls.core.orNull

class PlsOpenAiChatModelProvider : PlsChatModelProvider {
    override val type: PlsChatModelType = PlsChatModelType.OPEN_AI

    override fun isAvailable(): Boolean {
        // modelName 和 apiEndpoint 为空时使用默认值
        // apiKey 为空时直接报错
        return true
    }

    override fun createChatModel(): OpenAiChatModel? {
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

    override fun createStreamingChatModel(): OpenAiStreamingChatModel? {
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
