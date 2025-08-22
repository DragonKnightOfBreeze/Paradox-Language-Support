package icu.windea.pls.ai.providers

import dev.langchain4j.model.chat.*
import dev.langchain4j.model.openai.*
import icu.windea.pls.ai.model.*

class OpenAiChatModelProvider : ChatModelProvider<OpenAiChatModelOptions> {
    override val type: ChatModelType = ChatModelType.OPEN_AI

    override val options: OpenAiChatModelOptions? get() = OpenAiChatModelOptions.get()

    override fun isAvailable(): Boolean {
        return true
    }

    override fun createChatModel(): OpenAiChatModel? {
        val options = options ?: return null
        return OpenAiChatModel.builder()
            .modelName(options.modelName)
            .baseUrl(options.apiEndpoint)
            .apiKey(options.apiKey)
            .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
            .strictJsonSchema(true)
            .build()
    }

    override fun createStreamingChatModel(): OpenAiStreamingChatModel? {
        val options = options ?: return null
        return OpenAiStreamingChatModel.builder()
            .modelName(options.modelName)
            .baseUrl(options.apiEndpoint)
            .apiKey(options.apiKey)
            .build()
    }
}
