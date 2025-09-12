package icu.windea.pls.ai.providers

import com.intellij.util.application
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.settings.PlsAiSettingsManager
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.OptionProvider

/**
 * ANTHROPIC 模型提供者。
 *
 * - 支持通过环境变量读取配置：
 *   - ANTHROPIC_MODEL（默认：claude-3-5-sonnet-latest）
 *   - ANTHROPIC_BASE_URL（默认：https://api.anthropic.com）
 *   - ANTHROPIC_API_KEY（必填）
 * - 单元测试模式下同样从环境变量读取。
 */
class AnthropicChatModelProvider : ChatModelProvider<AnthropicChatModelProvider.Options> {
    override val type: ChatModelProviderType = ChatModelProviderType.ANTHROPIC

    override val options: Options? get() = Options.get()

    override fun getChatModel(): AnthropicChatModel? {
        val opts = options ?: return null
        ensureCache(opts)
        if (cachedChatModel == null) {
            cachedChatModel = AnthropicChatModel.builder()
                .modelName(opts.modelName)
                .baseUrl(opts.apiEndpoint)
                .apiKey(opts.apiKey)
                .build()
        }
        return cachedChatModel
    }

    override fun getStreamingChatModel(): AnthropicStreamingChatModel? {
        val opts = options ?: return null
        ensureCache(opts)
        if (cachedStreamingChatModel == null) {
            cachedStreamingChatModel = AnthropicStreamingChatModel.builder()
                .modelName(opts.modelName)
                .baseUrl(opts.apiEndpoint)
                .apiKey(opts.apiKey)
                .build()
        }
        return cachedStreamingChatModel
    }

    override fun isAvailable(): Boolean = options != null

    @Volatile private var cachedOptions: Options? = null
    @Volatile private var cachedChatModel: AnthropicChatModel? = null
    @Volatile private var cachedStreamingChatModel: AnthropicStreamingChatModel? = null

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
