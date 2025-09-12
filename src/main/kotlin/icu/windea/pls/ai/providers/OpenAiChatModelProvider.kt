package icu.windea.pls.ai.providers

import com.intellij.util.application
import dev.langchain4j.model.chat.Capability
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.settings.PlsAiSettingsManager
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.OptionProvider

class OpenAiChatModelProvider : ChatModelProviderBase<OpenAiChatModelProvider.Options>() {
    override val type: ChatModelProviderType = ChatModelProviderType.OPEN_AI

    override val options: Options? get() = Options.get()

    override fun doGetChatModel(options: Options): OpenAiChatModel? {
        return OpenAiChatModel.builder()
            .modelName(options.modelName)
            .baseUrl(options.apiEndpoint)
            .apiKey(options.apiKey)
            .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
            .strictJsonSchema(true)
            .build()
    }

    override fun doGetStreamingChatModel(options: Options): OpenAiStreamingChatModel? {
        return OpenAiStreamingChatModel.builder()
            .modelName(options.modelName)
            .baseUrl(options.apiEndpoint)
            .apiKey(options.apiKey)
            .strictJsonSchema(true)
            .build()
    }

    override fun doCHeckStatus(): ChatModelProvider.StatusResult {
        TODO("Not yet implemented")
    }

    data class Options(
        val modelName: String,
        val apiEndpoint: String,
        val apiKey: String
    ) : ChatModelProvider.Options {
        companion object {
            fun get(): Options? {
                return when {
                    application.isUnitTestMode -> forUnitTest()
                    else -> fromSettings()
                }
            }

            fun forUnitTest(): Options? {
                val modelName = "deepseek-chat"
                val apiEndpoint = "https://api.deepseek.com"
                val apiKey = System.getenv("DEEPSEEK_KEY")?.orNull() ?: return null
                return Options(modelName, apiEndpoint, apiKey)
            }

            fun fromSettings(): Options? {
                val settings = PlsAiFacade.getSettings().openAI
                val modelName = OptionProvider.from(settings.modelName, PlsAiSettingsManager.defaultOpenAiModelName)
                    .fromEnv(settings.fromEnv, settings.modelNameEnv, PlsAiSettingsManager.defaultOpenAiModelNameEnv)
                    .get()
                val apiEndpoint = OptionProvider.from(settings.apiEndpoint, PlsAiSettingsManager.defaultOpenAiApiEndpoint)
                    .fromEnv(settings.fromEnv, settings.apiEndpointEnv, PlsAiSettingsManager.defaultOpenAiApiEndpointEnv)
                    .get()
                val apiKey = OptionProvider.from(settings.apiKey, null)
                    .fromEnv(settings.fromEnv, settings.apiKeyEnv, PlsAiSettingsManager.defaultOpenAiApiKeyEnv)
                    .get()
                if (apiKey == null) return null
                return Options(modelName, apiEndpoint, apiKey)
            }
        }
    }
}
