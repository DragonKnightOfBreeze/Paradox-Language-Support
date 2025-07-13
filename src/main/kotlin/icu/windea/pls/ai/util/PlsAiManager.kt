package icu.windea.pls.ai.util

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.openapi.components.*
import dev.langchain4j.exception.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.services.*
import icu.windea.pls.ai.settings.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*

object PlsAiManager {
    fun getSettings(): PlsAiSettingsState = service<PlsAiSettings>().state

    fun getChatModelTypeToUse(): PlsChatModelType {
        return PlsChatModelType.OPEN_AI
    }

    fun isEnabled(): Boolean = getSettings().enable

    fun isAvailable(): Boolean = isEnabled() && PlsAiSettingsManager.isValid()

    fun getTranslateLocalisationService() = service<PlsAiTranslateLocalisationService>()

    fun getPolishLocalisationService() = service<PlsAiPolishLocalisationService>()

    fun getOptimizedErrorMessage(e: Throwable): String? {
        val message = e.message
        when (e) {
            is LangChain4jException -> {
                if (message.isNotNullOrEmpty()) {
                    runCatchingCancelable {
                        val errorInfo = ObjectMappers.jsonMapper.readValue<OpenAiErrorInfo>(message)
                        return "[${errorInfo.error.code}] ${errorInfo.error.message}"
                    }
                }
                return e.message
            }
            else -> return e.message
        }
    }
}
