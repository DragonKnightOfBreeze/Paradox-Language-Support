package icu.windea.pls.ai.settings

import com.intellij.DynamicBundle
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.layout.ValidationInfoBuilder
import icu.windea.pls.PlsBundle
import java.util.*

object PlsAiSettingsManager {
    const val defaultLocalisationChunkSize: Int = 100
    const val defaultLocalisationMemorySize: Int = 10000

    // 通用（单测场景）
    val defaultModelNameInUnitTest: String get() = "deepseek-chat"
    val defaultApiEndpointInUnitTest: String get() = "https://api.deepseek.com"

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

    fun validateOpenAiApiKey(builder: ValidationInfoBuilder, field: JBPasswordField): ValidationInfo? {
        // 目前仅在输入时验证，不在应用时验证
        // 如果启用 AI 集成，但是这里的验证并未通过，相关功能仍然可用，只是使用后会给出警告
        if (field.password.isEmpty()) return builder.warning(PlsBundle.message("settings.ai.openAI.apiKey.1"))
        return null
    }

    // endregion

    // region Anthropic

    // 基于 IDE 界面语言
    val defaultAnthropicModelName: String
        get() = if (DynamicBundle.getLocale() == Locale.SIMPLIFIED_CHINESE) "deepseek-chat" else "claude-3-5-sonnet-latest"
    // 基于 IDE 界面语言
    val defaultAnthropicApiEndpoint: String
        get() = if (DynamicBundle.getLocale() == Locale.SIMPLIFIED_CHINESE) "https://api.deepseek.com" else "https://api.anthropic.com"

    const val defaultAnthropicModelNameEnv: String = "ANTHROPIC_MODEL"
    const val defaultAnthropicApiEndpointEnv: String = "ANTHROPIC_BASE_URL"
    const val defaultAnthropicApiKeyEnv: String = "ANTHROPIC_API_KEY"

    fun validateAnthropicApiKey(builder: ValidationInfoBuilder, field: JBPasswordField): ValidationInfo? {
        if (field.password.isEmpty()) return builder.warning(PlsBundle.message("settings.ai.openAI.apiKey.1"))
        return null
    }

    // endregion

    // region Local (Ollama)

    const val defaultLocalApiEndpoint: String = "http://localhost:11434"

    const val defaultLocalModelNameEnv: String = "OLLAMA_MODEL"
    const val defaultLocalApiEndpointEnv: String = "OLLAMA_BASE_URL"

    // endregion
}
