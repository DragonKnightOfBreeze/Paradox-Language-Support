package icu.windea.pls.ai.providers

import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.platform.ide.progress.ModalTaskOwner
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.util.io.HttpRequests
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
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

        val baseUrl = options.apiEndpoint.trimEnd('/')
        val ref = AtomicReference<StatusResult>()

        // 使用 IDE 自身的代理设置和固定的超时，发出 HTTP 请求
        val action: suspend CoroutineScope.() -> Unit = action@{
            checkApiVersion(baseUrl, ref)
            if (ref.get() != null) return@action
            checkApiTags(options, baseUrl, ref)
            if (ref.get() != null) return@action
            checkHelloWorld(options, baseUrl, ref)
        }
        when {
            ChronicleFacade.isUnitTestMode() -> runBlocking { action() }
            else -> runWithModalProgressBlocking(ModalTaskOwner.guess(), ChronicleAiBundle.message("ai.test.progress.title")) { action() }
        }

        ref.get()?.let { return it }
        return StatusResult(true, ChronicleAiBundle.message("ai.test.success.title"), ChronicleAiBundle.message("ai.test.success.service", baseUrl))
    }

    private suspend fun checkApiVersion(baseUrl: String, ref: AtomicReference<StatusResult>) {
        // check /api/version api
        withContext(Dispatchers.IO) {
            val versionUrl = "$baseUrl/api/version"
            try {
                HttpRequests.request(versionUrl).useProxy(true).connectTimeout(3000).readTimeout(3000).tryConnect()
            } catch (_: Exception) {
                val r = StatusResult(false, ChronicleAiBundle.message("ai.test.error.title"), ChronicleAiBundle.message("ai.test.error.local.unreachable", versionUrl))
                ref.set(r)
            }
        }
    }

    private suspend fun checkApiTags(options: Options, baseUrl: String, ref: AtomicReference<StatusResult>) {
        // check /api/tags api
        withContext(Dispatchers.IO) {
            val tagsUrl = "$baseUrl/api/tags"
            try {
                val text = HttpRequests.request(tagsUrl).useProxy(true).connectTimeout(3000).readTimeout(3000).readString()
                val n1 = "\"name\":\"${options.modelName}\""
                val n2 = "\"model\":\"${options.modelName}\""
                val isValid = text.contains(n1) || text.contains(n2)
                if (!isValid) throw IllegalStateException()
            } catch (_: Exception) {
                val r = StatusResult(false, ChronicleAiBundle.message("ai.test.error.title"), ChronicleAiBundle.message("ai.test.error.local.modelMissing", options.modelName))
                ref.set(r)
            }
        }
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
        @JvmField val INSTANCE = LocalChatModelProvider()
    }

    data class Options(
        val modelName: String,
        val apiEndpoint: String,
    ) : ChatModelProvider.Options {
        companion object {
            fun get(): Options? {
                return when {
                    ChronicleFacade.isUnitTestMode() -> forUnitTest()
                    else -> fromSettings()
                }
            }

            fun forUnitTest(): Options? {
                val modelName = System.getenv(AiConstants.Local.defaultModelEnv)?.orNull()
                    ?: return null
                val apiEndpoint = System.getenv(AiConstants.Local.defaultBaseUrlEnv)?.orNull()
                    ?: return null
                return Options(modelName, apiEndpoint)
            }

            fun fromProperties(properties: Properties): Options? {
                val modelName = OptionProvider.from(properties.modelName, null)
                    .fromEnv(properties.fromEnv, properties.modelNameEnv, AiConstants.Local.defaultModelEnv)
                    .get()
                val apiEndpoint = OptionProvider.from(properties.apiEndpoint, AiConstants.Local.defaultBaseUrl)
                    .fromEnv(properties.fromEnv, properties.apiEndpointEnv, AiConstants.Local.defaultBaseUrlEnv)
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
            private val settings = PlsAiSettings.getInstance().state.local

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
