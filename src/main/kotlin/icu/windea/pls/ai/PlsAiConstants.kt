package icu.windea.pls.ai

import com.intellij.DynamicBundle
import java.util.*

object PlsAiConstants {
    object Settings {
        const val defaultLocalisationChunkSize: Int = 100
        const val defaultLocalisationMemorySize: Int = 10000
    }

    object OpenAi {
        // 基于 IDE 界面语言
        val defaultModelName: String get() = fromLocale("deepseek-chat", "gpt-4o-mini")
        // 基于 IDE 界面语言
        val defaultApiEndpoint: String get() = fromLocale("https://api.deepseek.com", "https://api.openai.com/v1")

        const val defaultModelNameEnv: String = "OPENAI_MODEL"
        const val defaultApiEndpointEnv: String = "OPENAI_BASE_URL"
        const val defaultApiKeyEnv: String = "OPENAI_API_KEY"
    }

    object Anthropic {
        val defaultModelName: String get() = fromLocale("deepseek-chat", "claude-3-5-sonnet-latest")
        val defaultApiEndpoint: String get() = fromLocale("https://api.deepseek.com/anthropic", "https://api.anthropic.com/v1")

        const val defaultModelNameEnv: String = "ANTHROPIC_MODEL"
        const val defaultApiEndpointEnv: String = "ANTHROPIC_BASE_URL"
        const val defaultApiKeyEnv: String = "ANTHROPIC_API_KEY"
    }

    object Local {
        const val defaultApiEndpoint: String = "http://localhost:11434"

        const val defaultModelNameEnv: String = "OLLAMA_MODEL"
        const val defaultApiEndpointEnv: String = "OLLAMA_BASE_URL"
    }

    private fun fromLocale(v1: String, v2: String): String {
        return when (DynamicBundle.getLocale()) {
            Locale.SIMPLIFIED_CHINESE -> v1
            else -> v2
        }
    }
}
