package icu.windea.pls.ai.model

import com.intellij.DynamicBundle
import java.util.*

object AiConstants {
    @Suppress("unused")
    object Models {
        const val deepseekFlash = "deepseek-v4-flash"
        const val deepseekPro = "deepseek-v4-pro"
        const val claudeHaiku = "claude-haiku-5.0"
        const val claudeSonnet = "claude-sonnet-5.0"
        const val claudeOpus = "claude-opus-5.0"
        const val gptMini = "gpt-5.6-lunar"
        const val gpt = "gpt-5.6"
    }

    object BaseUrls {
        const val deepseek = "https://api.deepseek.com"
        const val deepseekAnthropic = "https://api.deepseek.com/anthropic"
        const val openAi = "https://api.openai.com/v1"
        const val anthropic = "https://api.anthropic.com/v1"
    }

    object OpenAi {
        const val defaultModel: String = Models.gptMini
        const val defaultBaseUrl: String = BaseUrls.openAi

        val defaultModelFromLocale: String get() = fromLocale(Models.deepseekFlash, defaultModel)
        val defaultBaseUrlFromLocale: String get() = fromLocale(BaseUrls.deepseek, defaultBaseUrl)

        const val defaultModelEnv: String = "OPENAI_MODEL"
        const val defaultBaseUrlEnv: String = "OPENAI_BASE_URL"
        const val defaultApiKeyEnv: String = "OPENAI_API_KEY"
    }

    object Anthropic {
        const val defaultModel: String = Models.claudeHaiku
        const val defaultBaseUrl = BaseUrls.anthropic

        val defaultModelFromLocale: String get() = fromLocale(Models.deepseekFlash, defaultModel)
        val defaultBaseUrlFromLocale: String get() = fromLocale(BaseUrls.deepseekAnthropic, defaultBaseUrl)

        const val defaultModelEnv: String = "ANTHROPIC_MODEL"
        const val defaultBaseUrlEnv: String = "ANTHROPIC_BASE_URL"
        const val defaultApiKeyEnv: String = "ANTHROPIC_API_KEY"
    }

    object Local {
        const val defaultBaseUrl: String = "http://localhost:11434"

        const val defaultModelEnv: String = "OLLAMA_MODEL"
        const val defaultBaseUrlEnv: String = "OLLAMA_BASE_URL"
    }

    private fun fromLocale(v1: String, v2: String): String {
        return when (DynamicBundle.getLocale()) {
            Locale.SIMPLIFIED_CHINESE -> v1
            else -> v2
        }
    }
}
