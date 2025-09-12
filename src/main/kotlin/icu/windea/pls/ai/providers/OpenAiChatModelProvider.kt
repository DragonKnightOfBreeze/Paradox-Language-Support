package icu.windea.pls.ai.providers

import com.intellij.util.application
import dev.langchain4j.model.chat.Capability
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.settings.PlsAiSettingsManager
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.OptionProvider

class OpenAiChatModelProvider : ChatModelProvider<OpenAiChatModelProvider.Options> {
    override val type: ChatModelProviderType = ChatModelProviderType.OPEN_AI

    override val options: Options? get() = Options.get()

    override fun getChatModel(): OpenAiChatModel? {
        val opts = options ?: return null
        ensureCache(opts)
        if (cachedChatModel == null) {
            cachedChatModel = OpenAiChatModel.builder()
                .modelName(opts.modelName)
                .baseUrl(opts.apiEndpoint)
                .apiKey(opts.apiKey)
                .supportedCapabilities(Capability.RESPONSE_FORMAT_JSON_SCHEMA)
                .strictJsonSchema(true)
                .build()
        }
        return cachedChatModel
    }

    override fun getStreamingChatModel(): OpenAiStreamingChatModel? {
        val opts = options ?: return null
        ensureCache(opts)
        if (cachedStreamingChatModel == null) {
            cachedStreamingChatModel = OpenAiStreamingChatModel.builder()
                .modelName(opts.modelName)
                .baseUrl(opts.apiEndpoint)
                .apiKey(opts.apiKey)
                .strictJsonSchema(true)
                .build()
        }
        return cachedStreamingChatModel
    }

    override fun isAvailable(): Boolean = options != null

    @Volatile private var cachedOptions: Options? = null
    @Volatile private var cachedChatModel: OpenAiChatModel? = null
    @Volatile private var cachedStreamingChatModel: OpenAiStreamingChatModel? = null

    private fun ensureCache(newOptions: Options) {
        if (cachedOptions != newOptions) {
            cachedOptions = newOptions
            cachedChatModel = null
            cachedStreamingChatModel = null
        }
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
