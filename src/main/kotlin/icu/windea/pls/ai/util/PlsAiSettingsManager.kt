package icu.windea.pls.ai.util

import com.intellij.*
import java.util.*

object PlsAiSettingsManager {
    fun getDefaultBatchSizeOfLocalisations(): Int {
        return 40
    }

    fun getDefaultOpenAiModelName(): String {
        //基于IDE界面语言
        return when (Locale.SIMPLIFIED_CHINESE) {
            DynamicBundle.getLocale() -> "deepseek-chat"
            else -> "gpt-4o-mini"
        }
    }

    fun getDefaultOpenAiApiEndpoint(): String {
        //基于IDE界面语言
        return when (Locale.SIMPLIFIED_CHINESE) {
            DynamicBundle.getLocale() -> "https://api.deepseek.com"
            else -> "https://api.openai.com"
        }
    }
}
