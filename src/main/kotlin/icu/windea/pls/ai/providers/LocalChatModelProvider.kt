package icu.windea.pls.ai.providers

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.io.HttpRequests
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.providers.ChatModelProvider.StatusResult
import icu.windea.pls.ai.PlsAiConstants
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.OptionProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

class LocalChatModelProvider : ChatModelProviderBase<LocalChatModelProvider.Options>() {
    override val type: ChatModelProviderType = ChatModelProviderType.LOCAL

    override val options: Options? get() = Options.get()

    override fun doGetChatModel(options: Options): OllamaChatModel {
        return OllamaChatModel.builder()
            .modelName(options.modelName)
            .baseUrl(options.apiEndpoint)
            .build()
    }

    override fun doGetStreamingChatModel(options: Options): OllamaStreamingChatModel {
        return OllamaStreamingChatModel.builder()
            .modelName(options.modelName)
            .baseUrl(options.apiEndpoint)
            .build()
    }

    override fun doCheckStatus(options: Options): StatusResult {
        // com.intellij.util.net.ProxySettingsUi.doCheckConnection
        // com.intellij.util.io.RequestBuilder.tryConnect

        val url = options.apiEndpoint.trimEnd('/')
        val versionUrl = "$url/api/version"
        val tagsUrl = "$url/api/tags"
        val ref = AtomicReference<StatusResult>()

        val action: suspend CoroutineScope.() -> Unit = action@{
            // 使用 IDE 自身的代理设置和固定的超时，发出 HTTP 请求

            // check /api/version api
            withContext(Dispatchers.IO) {
                try {
                    HttpRequests.request(versionUrl).useProxy(true)
                        .connectTimeout(3000).readTimeout(3000)
                        .tryConnect()
                } catch (_: Exception) {
                    val r = StatusResult(false, PlsBundle.message("ai.test.error.title"), PlsBundle.message("ai.test.error.local.unreachable", versionUrl))
                    ref.set(r)
                }
            }
            // check /api/tags api
            if (ref.get() != null) return@action
            withContext(Dispatchers.IO) {
                try {
                    val text = HttpRequests.request(tagsUrl).useProxy(true)
                        .connectTimeout(3000).readTimeout(3000)
                        .readString()
                    val n1 = "\"name\":\"${options.modelName}\""
                    val n2 = "\"model\":\"${options.modelName}\""
                    val isValid = text.contains(n1) || text.contains(n2)
                    if (!isValid) throw IllegalStateException()
                } catch (_: Exception) {
                    val r = StatusResult(false, PlsBundle.message("ai.test.error.title"), PlsBundle.message("ai.test.error.local.modelMissing", options.modelName))
                    ref.set(r)
                }
            }
            // say hello world
            if (ref.get() != null) return@action
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
        @JvmStatic val INSTANCE = LocalChatModelProvider()
    }

    data class Options(
        val modelName: String,
        val apiEndpoint: String,
    ) : ChatModelProvider.Options {
        companion object {
            fun get(): Options? {
                return when {
                    PlsFacade.isUnitTestMode() -> forUnitTest()
                    else -> fromSettings()
                }
            }

            fun forUnitTest(): Options? {
                val modelName = System.getenv("OLLAMA_MODEL")?.orNull() ?: return null
                val apiEndpoint = System.getenv("OLLAMA_BASE_URL")?.orNull() ?: return null
                return Options(modelName, apiEndpoint)
            }

            fun fromProperties(properties: Properties): Options? {
                val modelName = OptionProvider.from(properties.modelName, null)
                    .fromEnv(properties.fromEnv, properties.modelNameEnv, PlsAiConstants.Local.defaultModelNameEnv)
                    .get()
                val apiEndpoint = OptionProvider.from(properties.apiEndpoint, PlsAiConstants.Local.defaultApiEndpoint)
                    .fromEnv(properties.fromEnv, properties.apiEndpointEnv, PlsAiConstants.Local.defaultApiEndpointEnv)
                    .get()
                if (modelName == null) return null
                return Options(modelName, apiEndpoint)
            }

            fun fromSettings(): Options? {
                return fromProperties(AtomicProperties().toProperties())
            }
        }

        data class Properties(
            val modelName: String? = null,
            val apiEndpoint: String? = null,
            val fromEnv: Boolean = false,
            val modelNameEnv: String? = null,
            val apiEndpointEnv: String? = null,
        )

        class AtomicProperties {
            private val settings = PlsAiFacade.getSettings().local

            val modelName = AtomicProperty(settings.modelName.orEmpty())
            val apiEndpoint = AtomicProperty(settings.apiEndpoint.orEmpty())
            val fromEnv = AtomicProperty(settings.fromEnv)
            val modelNameEnv = AtomicProperty(settings.modelNameEnv.orEmpty())
            val apiEndpointEnv = AtomicProperty(settings.apiEndpointEnv.orEmpty())

            fun toProperties(): Properties {
                return Properties(
                    modelName = modelName.get(),
                    apiEndpoint = apiEndpoint.get(),
                    fromEnv = fromEnv.get(),
                    modelNameEnv = modelNameEnv.get(),
                    apiEndpointEnv = apiEndpointEnv.get(),
                )
            }
        }
    }
}
