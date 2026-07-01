package icu.windea.pls.ai.providers

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.ai.AiConstants
import icu.windea.pls.ai.ChronicleAiBundle
import icu.windea.pls.ai.providers.ChatModelProvider.*
import icu.windea.pls.ai.settings.PlsAiSettings
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.OptionProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

class AnthropicChatModelProvider : ChatModelProviderBase<AnthropicChatModelProvider.Options>() {
    override val type: ChatModelProviderType = ChatModelProviderType.ANTHROPIC

    override val options: Options? get() = Options.get()

    override fun doGetChatModel(options: Options): AnthropicChatModel {
        return AnthropicChatModel.builder()
            .modelName(options.modelName)
            .baseUrl(options.apiEndpoint)
            .apiKey(options.apiKey)
            .build()
    }

    override fun doGetStreamingChatModel(options: Options): AnthropicStreamingChatModel {
        return AnthropicStreamingChatModel.builder()
            .modelName(options.modelName)
            .baseUrl(options.apiEndpoint)
            .apiKey(options.apiKey)
            .build()
    }

    override fun doCheckStatus(options: Options): StatusResult {
        val baseUrl = options.apiEndpoint.trimEnd('/')
        val ref = AtomicReference<StatusResult>()

        // 使用 IDE 自身的代理设置和固定的超时，发出 HTTP 请求
        val action: suspend CoroutineScope.() -> Unit = {
            checkHelloWorld(options, baseUrl, ref)
        }
        when {
            ChronicleFacade.isUnitTestMode() -> runBlocking { action() }
            else -> runWithModalProgressBlocking(ModalTaskOwner.guess(), ChronicleAiBundle.message("ai.test.progress.title")) { action() }
        }

        ref.get()?.let { return it }
        return StatusResult(true, ChronicleAiBundle.message("ai.test.success.title"), ChronicleAiBundle.message("ai.test.success.service", baseUrl))
    }

    private suspend fun checkHelloWorld(options: Options, baseUrl: String, ref: AtomicReference<StatusResult>) {
        withContext(Dispatchers.IO) {
            try {
                val chatModel = doGetChatModel(options)
                chatModel.chat("Say 'hello world'")
            } catch (e: Exception) {
                val r = StatusResult(false, ChronicleAiBundle.message("ai.test.error.title"), ChronicleAiBundle.message("ai.test.error.service", baseUrl, e.message.orEmpty()))
                ref.set(r)
            }
        }
    }

    companion object {
        @JvmField val INSTANCE = AnthropicChatModelProvider()
    }

    data class Options(
        val modelName: String,
        val apiEndpoint: String,
        val apiKey: String,
    ) : ChatModelProvider.Options {
        companion object {
            fun get(): Options? {
                return when {
                    ChronicleFacade.isUnitTestMode() -> forUnitTest()
                    else -> fromSettings()
                }
            }

            fun forUnitTest(): Options? {
                val modelName = System.getenv(AiConstants.Anthropic.defaultModelEnv)?.orNull()
                    ?: AiConstants.Anthropic.defaultModel
                val apiEndpoint = System.getenv(AiConstants.Anthropic.defaultBaseUrlEnv)?.orNull()
                    ?: AiConstants.Anthropic.defaultBaseUrl
                val apiKey = System.getenv(AiConstants.Anthropic.defaultApiKeyEnv)?.orNull()
                    ?: return null
                return Options(modelName, apiEndpoint, apiKey)
            }

            fun fromProperties(properties: Properties): Options? {
                val modelName = OptionProvider.from(properties.modelName, AiConstants.Anthropic.defaultModelFromLocale)
                    .fromEnv(properties.fromEnv, properties.modelNameEnv, AiConstants.Anthropic.defaultModelEnv)
                    .get()
                val apiEndpoint = OptionProvider.from(properties.apiEndpoint, AiConstants.Anthropic.defaultBaseUrlFromLocale)
                    .fromEnv(properties.fromEnv, properties.apiEndpointEnv, AiConstants.Anthropic.defaultBaseUrlEnv)
                    .get()
                val apiKey = OptionProvider.from(properties.apiKey, null)
                    .fromEnv(properties.fromEnv, properties.apiKeyEnv, AiConstants.Anthropic.defaultApiKeyEnv)
                    .get()
                if (apiKey == null) return null
                return Options(modelName, apiEndpoint, apiKey)
            }

            fun fromSettings(): Options? {
                return fromProperties(AtomicProperties().toProperties())
            }
        }

        data class Properties(
            val modelName: String? = null,
            val apiEndpoint: String? = null,
            val apiKey: String? = null,
            val fromEnv: Boolean = false,
            val modelNameEnv: String? = null,
            val apiEndpointEnv: String? = null,
            val apiKeyEnv: String? = null,
        )

        class AtomicProperties {
            private val settings = PlsAiSettings.getInstance().state.anthropic

            val modelName = AtomicProperty(settings.modelName.orEmpty())
            val apiEndpoint = AtomicProperty(settings.apiEndpoint.orEmpty())
            val apiKey = AtomicProperty(settings.apiKey.orEmpty())
            val fromEnv = AtomicProperty(settings.fromEnv)
            val modelNameEnv = AtomicProperty(settings.modelNameEnv.orEmpty())
            val apiEndpointEnv = AtomicProperty(settings.apiEndpointEnv.orEmpty())
            val apiKeyEnv = AtomicProperty(settings.apiKeyEnv.orEmpty())

            fun toProperties(): Properties {
                return Properties(
                    modelName = modelName.get(),
                    apiEndpoint = apiEndpoint.get(),
                    apiKey = apiKey.get(),
                    fromEnv = fromEnv.get(),
                    modelNameEnv = modelNameEnv.get(),
                    apiEndpointEnv = apiEndpointEnv.get(),
                    apiKeyEnv = apiKeyEnv.get(),
                )
            }
        }
    }
}
