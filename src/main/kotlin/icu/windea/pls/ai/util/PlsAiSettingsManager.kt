package icu.windea.pls.ai.util

import com.intellij.*
import icu.windea.pls.ai.*
import icu.windea.pls.core.*
import java.util.*

object PlsAiSettingsManager {
    fun getChatModelTypeToUse(): PlsChatModelType {
        return PlsChatModelType.OPEN_AI
    }

    fun isValid(type: PlsChatModelType = getChatModelTypeToUse()): Boolean {
        return when (type) {
            PlsChatModelType.OPEN_AI -> isValidForOpenAI()
        }
    }

    private fun isValidForOpenAI(): Boolean {
        // modelName 和 apiEndpoint 为空时使用默认值
        // apiKey 为空时直接报错
        return true
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
