package icu.windea.pls.ai.providers

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import dev.langchain4j.model.anthropic.AnthropicChatModel
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ai.PlsAiConstants
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.providers.ChatModelProvider.StatusResult
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
        val url = options.apiEndpoint.trimEnd('/')
        val ref = AtomicReference<StatusResult>()

        val action: suspend CoroutineScope.() -> Unit = {
            // 弹出可取消的模态框，调用聊天方法

            // say hello world
            withContext(Dispatchers.IO) {
                try {
                    val chatModel = doGetChatModel(options)
                    chatModel.chat("Say 'hello world'")
                } catch (e: Exception) {
                    val r = StatusResult(false, PlsBundle.message("ai.test.error.title"), PlsBundle.message("ai.test.error.service", url, e.message.orEmpty()))
                    ref.set(r)
                }
            }
        }
        when {
            PlsFacade.isUnitTestMode() -> runBlocking { action() }
            else -> runWithModalProgressBlocking(ModalTaskOwner.guess(), PlsBundle.message("ai.test.progress.title")) { action() }
        }

        ref.get()?.let { return it }
        return StatusResult(true, PlsBundle.message("ai.test.success.title"), PlsBundle.message("ai.test.success.service", url))
    }

    companion object {
        @JvmStatic val INSTANCE = AnthropicChatModelProvider()
    }

    data class Options(
        val modelName: String,
        val apiEndpoint: String,
        val apiKey: String,
    ) : ChatModelProvider.Options {
        companion object {
            fun get(): Options? {
                return when {
                    PlsFacade.isUnitTestMode() -> forUnitTest()
                    else -> fromSettings()
                }
            }

            fun forUnitTest(): Options? {
                val modelName = "deepseek-chat"
                val apiEndpoint = "https://api.deepseek.com/anthropic"
                val apiKey = System.getenv("DEEPSEEK_KEY")?.orNull() ?: return null
                return Options(modelName, apiEndpoint, apiKey)
            }

            fun fromProperties(properties: Properties): Options? {
                val modelName = OptionProvider.from(properties.modelName, PlsAiConstants.Anthropic.defaultModelName)
                    .fromEnv(properties.fromEnv, properties.modelNameEnv, PlsAiConstants.Anthropic.defaultModelNameEnv)
                    .get()
                val apiEndpoint = OptionProvider.from(properties.apiEndpoint, PlsAiConstants.Anthropic.defaultApiEndpoint)
                    .fromEnv(properties.fromEnv, properties.apiEndpointEnv, PlsAiConstants.Anthropic.defaultApiEndpointEnv)
                    .get()
                val apiKey = OptionProvider.from(properties.apiKey, null)
                    .fromEnv(properties.fromEnv, properties.apiKeyEnv, PlsAiConstants.Anthropic.defaultApiKeyEnv)
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
            private val settings = PlsAiFacade.getSettings().anthropic

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
