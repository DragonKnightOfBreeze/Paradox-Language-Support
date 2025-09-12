package icu.windea.pls.ai.providers

import com.intellij.notification.NotificationType
import com.intellij.util.application
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import icu.windea.pls.PlsBundle
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.settings.PlsAiSettingsManager
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.OptionProvider
import icu.windea.pls.lang.util.PlsCoreManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * 本地（Ollama）模型提供者。
 *
 * - 通过环境变量读取配置：
 *   - OLLAMA_MODEL（必填，例如：qwen2.5:7b-instruct-q4_K_M 或 llama3.1:8b）
 *   - OLLAMA_BASE_URL（默认：http://localhost:11434）
 * - 单元测试模式下同样从环境变量读取。
 */
class LocalChatModelProvider : ChatModelProvider<LocalChatModelProvider.Options> {
    override val type: ChatModelProviderType = ChatModelProviderType.LOCAL

    override val options: Options? get() = Options.get()

    override fun getChatModel(): OllamaChatModel? {
        val opts = options ?: return null
        ensureCache(opts)
        if (cachedChatModel == null) {
            cachedChatModel = OllamaChatModel.builder()
                .modelName(opts.modelName)
                .baseUrl(opts.apiEndpoint)
                .build()
        }
        return cachedChatModel
    }

    override fun getStreamingChatModel(): OllamaStreamingChatModel? {
        val opts = options ?: return null
        ensureCache(opts)
        if (cachedStreamingChatModel == null) {
            cachedStreamingChatModel = OllamaStreamingChatModel.builder()
                .modelName(opts.modelName)
                .baseUrl(opts.apiEndpoint)
                .build()
        }
        return cachedStreamingChatModel
    }

    override fun isAvailable(): Boolean = options != null

    @Volatile private var cachedOptions: Options? = null
    @Volatile private var cachedChatModel: OllamaChatModel? = null
    @Volatile private var cachedStreamingChatModel: OllamaStreamingChatModel? = null

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
    ) : ChatModelProvider.Options {
        companion object {
            fun get(): Options? {
                return when {
                    application.isUnitTestMode -> forUnitTest()
                    else -> fromSettings()
                }
            }

            fun forUnitTest(): Options? {
                val modelName = System.getenv("OLLAMA_MODEL")?.orNull() ?: return null
                val apiEndpoint = System.getenv("OLLAMA_BASE_URL")?.orNull() ?: return null
                return Options(modelName, apiEndpoint)
            }

            fun fromSettings(): Options? {
                val settings = PlsAiFacade.getSettings().local
                val modelName = OptionProvider.from(settings.modelName, null)
                    .fromEnv(settings.fromEnv, settings.modelNameEnv, PlsAiSettingsManager.defaultLocalModelNameEnv)
                    .get()
                val apiEndpoint = OptionProvider.from(settings.apiEndpoint, PlsAiSettingsManager.defaultLocalApiEndpoint)
                    .fromEnv(settings.fromEnv, settings.apiEndpointEnv, PlsAiSettingsManager.defaultLocalApiEndpointEnv)
                    .get()
                if (modelName == null) return null
                return Options(modelName, apiEndpoint)
            }
        }
    }

    private fun healthCheck(options: Options): Boolean {
        // 1) Check base URL reachable
        val versionUrl = options.apiEndpoint.trimEnd('/') + "/api/version"
        try {
            val conn = URL(versionUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 2000
            conn.readTimeout = 2000
            val code = conn.responseCode
            if (code !in 200..299) {
                PlsCoreManager.createNotification(
                    NotificationType.WARNING,
                    PlsBundle.message("ai.local.health.unreachable", "$code")
                ).notify(null)
                return false
            }
        } catch (e: Exception) {
            PlsCoreManager.createNotification(
                NotificationType.WARNING,
                PlsBundle.message("ai.local.health.unreachable", e.message ?: "")
            ).notify(null)
            return false
        }

        // 2) Check model installed
        val tagsUrl = options.apiEndpoint.trimEnd('/') + "/api/tags"
        try {
            val conn = URL(tagsUrl).openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 2000
            conn.readTimeout = 2000
            val code = conn.responseCode
            if (code in 200..299) {
                val content = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val needle1 = "\"name\":\"${options.modelName}\""
                val needle2 = "\"model\":\"${options.modelName}\""
                if (needle1 !in content && needle2 !in content) {
                    PlsCoreManager.createNotification(
                        NotificationType.WARNING,
                        PlsBundle.message("ai.local.health.modelMissing", options.modelName)
                    ).notify(null)
                    return false
                }
            }
        } catch (_: Exception) {
            // ignore errors for tags endpoint, since version already passed. We don't fail fast here.
        }

        return true
    }
}
