package icu.windea.pls.ai.util

import com.intellij.openapi.components.*
import com.intellij.openapi.diagnostic.*
import icu.windea.pls.ai.PlsChatModelType
import icu.windea.pls.ai.settings.*
import java.lang.invoke.*

object PlsAiManager {
    fun getSettings(): PlsAiSettingsState = service<PlsAiSettings>().state

    fun getChatModelTypeToUse(): PlsChatModelType {
        return PlsChatModelType.OPEN_AI
    }

    fun isEnabled(): Boolean = getSettings().enable

    fun isAvailable(): Boolean = isEnabled() && isValid()

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
}
