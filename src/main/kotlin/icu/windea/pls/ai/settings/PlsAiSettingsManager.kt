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
}
