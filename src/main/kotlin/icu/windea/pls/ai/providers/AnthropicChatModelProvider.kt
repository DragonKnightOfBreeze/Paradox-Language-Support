package icu.windea.pls.ai.providers

import com.intellij.util.application
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.settings.PlsAiSettingsManager
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.OptionProvider

class AnthropicChatModelProvider : ChatModelProviderBase<AnthropicChatModelProvider.Options>() {
    override val type: ChatModelProviderType = ChatModelProviderType.ANTHROPIC

    override val options: Options? get() = Options.get()

    override fun doGetChatModel(options: Options): AnthropicChatModel? {
        return AnthropicChatModel.builder()
            .modelName(options.modelName)
            .baseUrl(options.apiEndpoint)
            .apiKey(options.apiKey)
            .build()
    }

    override fun doGetStreamingChatModel(options: Options): AnthropicStreamingChatModel? {
        return AnthropicStreamingChatModel.builder()
            .modelName(options.modelName)
            .baseUrl(options.apiEndpoint)
            .apiKey(options.apiKey)
            .build()
    }

    override fun doCHeckStatus(): ChatModelProvider.StatusResult {
        TODO("Not yet implemented")
    }

    data class Options(
        val modelName: String,
        val apiEndpoint: String,
        val apiKey: String,
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
                val apiEndpoint = "https://api.deepseek.com/anthropic"
                val apiKey = System.getenv("DEEPSEEK_KEY")?.orNull() ?: return null
                return Options(modelName, apiEndpoint, apiKey)
            }

            fun fromSettings(): Options? {
                val settings = PlsAiFacade.getSettings().anthropic
                val modelName = OptionProvider.from(settings.modelName, PlsAiSettingsManager.defaultAnthropicModelName)
                    .fromEnv(settings.fromEnv, settings.modelNameEnv, PlsAiSettingsManager.defaultAnthropicModelNameEnv)
                    .get()
                val apiEndpoint = OptionProvider.from(settings.apiEndpoint, PlsAiSettingsManager.defaultAnthropicApiEndpoint)
                    .fromEnv(settings.fromEnv, settings.apiEndpointEnv, PlsAiSettingsManager.defaultAnthropicApiEndpointEnv)
                    .get()
                val apiKey = OptionProvider.from(settings.apiKey, null)
                    .fromEnv(settings.fromEnv, settings.apiKeyEnv, PlsAiSettingsManager.defaultAnthropicApiKeyEnv)
                    .get()
                if (apiKey == null) return null
                return Options(modelName, apiEndpoint, apiKey)
            }
        }
    }
}
