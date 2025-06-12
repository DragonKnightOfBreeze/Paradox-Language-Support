package icu.windea.pls.ai.util

import com.intellij.openapi.components.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.services.*
import icu.windea.pls.ai.settings.*

object PlsAiManager {
    fun getSettings(): PlsAiSettingsState = service<PlsAiSettings>().state

    fun getChatModelTypeToUse(): PlsChatModelType {
        return PlsChatModelType.OPEN_AI
    }

    fun isEnabled(): Boolean = getSettings().enable

    fun isAvailable(): Boolean = isEnabled() && PlsAiSettingsManager.isValid()

    fun getTranslateLocalisationService() = service<PlsAiTranslateLocalisationService>()

    fun getPolishLocalisationService() = service<PlsAiPolishLocalisationService>()
}
