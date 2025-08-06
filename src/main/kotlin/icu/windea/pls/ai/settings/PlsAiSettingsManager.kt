package icu.windea.pls.ai.settings

import com.intellij.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.util.PlsChatModelManager
import java.util.*

object PlsAiSettingsManager {
    fun getDefaultBatchSizeOfLocalisations(): Int = 50

    fun getMaxBatchSizeOfLocalisations(): Int = 1000

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
