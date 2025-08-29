package icu.windea.pls.ai

import com.intellij.openapi.components.service
import icu.windea.pls.ai.services.PolishLocalisationAiService
import icu.windea.pls.ai.services.TranslateLocalisationAiService
import icu.windea.pls.ai.settings.PlsAiSettings
import icu.windea.pls.ai.settings.PlsAiSettingsState
import icu.windea.pls.ai.util.PlsChatModelManager

object PlsAiFacade {
    fun getSettings(): PlsAiSettingsState = service<PlsAiSettings>().state

    fun isAvailable(): Boolean = getSettings().enable && PlsChatModelManager.isAvailable()

    fun getTranslateLocalisationService() = service<TranslateLocalisationAiService>()

    fun getPolishLocalisationService() = service<PolishLocalisationAiService>()
}
