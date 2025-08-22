package icu.windea.pls.ai.providers

import com.intellij.util.application
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.settings.PlsAiSettingsManager
import icu.windea.pls.core.orNull

class OpenAiChatModelOptions(
    val modelName: String,
    val apiEndpoint: String,
    val apiKey: String
): ChatModelOptions {
    companion object {
        const val ENV_KEY = "DEEPSEEK_KEY"

        fun get() : OpenAiChatModelOptions? {
            return when {
                application.isUnitTestMode -> forUnitTest()
                else -> fromSettings()
            }
        }

        fun forUnitTest() : OpenAiChatModelOptions? {
            val modelName = "deepseek-chat"
            val apiEndpoint = "https://api.deepseek.com"
            val apiKey = System.getenv(ENV_KEY)?.orNull() ?: return null
            return OpenAiChatModelOptions(modelName, apiEndpoint, apiKey)
        }

        fun fromSettings(): OpenAiChatModelOptions? {
            val settings = PlsAiFacade.getSettings().openAI
            val modelName = settings.modelName?.orNull() ?: PlsAiSettingsManager.getDefaultOpenAiModelName()
            val apiEndpoint = settings.apiEndpoint?.orNull() ?: PlsAiSettingsManager.getDefaultOpenAiApiEndpoint()
            val apiKey = settings.apiKey?.orNull() ?: return null
            return OpenAiChatModelOptions(modelName, apiEndpoint, apiKey)
        }
    }
}
