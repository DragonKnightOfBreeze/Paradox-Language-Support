package icu.windea.pls.ai.settings

import com.intellij.DynamicBundle
import java.util.*

object PlsAiSettingsManager {
    // region Feature Specific

    const val defaultLocalisationChunkSize: Int = 100
    const val defaultLocalisationMemorySize: Int = 10000

    // endregion

    // region Open AI

    // 基于 IDE 界面语言
    val defaultOpenAiModelName: String
        get() = if (DynamicBundle.getLocale() == Locale.SIMPLIFIED_CHINESE) "deepseek-chat" else "gpt-4o-mini"
    // 基于 IDE 界面语言
    val defaultOpenAiApiEndpoint: String
        get() = if (DynamicBundle.getLocale() == Locale.SIMPLIFIED_CHINESE) "https://api.deepseek.com" else "https://api.openai.com/v1"

    const val defaultOpenAiModelNameEnv: String = "OPENAI_MODEL"
    const val defaultOpenAiApiEndpointEnv: String = "OPENAI_BASE_URL"
    const val defaultOpenAiApiKeyEnv: String = "OPENAI_API_KEY"

    // endregion

    // region Anthropic

    // 基于 IDE 界面语言
    val defaultAnthropicModelName: String
        get() = if (DynamicBundle.getLocale() == Locale.SIMPLIFIED_CHINESE) "deepseek-chat" else "claude-3-5-sonnet-latest"
    // 基于 IDE 界面语言
    val defaultAnthropicApiEndpoint: String
        get() = if (DynamicBundle.getLocale() == Locale.SIMPLIFIED_CHINESE) "https://api.deepseek.com/anthropic" else "https://api.anthropic.com"

    const val defaultAnthropicModelNameEnv: String = "ANTHROPIC_MODEL"
    const val defaultAnthropicApiEndpointEnv: String = "ANTHROPIC_BASE_URL"
    const val defaultAnthropicApiKeyEnv: String = "ANTHROPIC_API_KEY"

    // endregion

    // region Local (Ollama)

    const val defaultLocalApiEndpoint: String = "http://localhost:11434"

    const val defaultLocalModelNameEnv: String = "OLLAMA_MODEL"
    const val defaultLocalApiEndpointEnv: String = "OLLAMA_BASE_URL"

    // endregion
}
