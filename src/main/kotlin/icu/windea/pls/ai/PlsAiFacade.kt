package icu.windea.pls.ai

import com.intellij.openapi.components.service
import icu.windea.pls.ai.services.PolishLocalisationAiService
import icu.windea.pls.ai.services.TranslateLocalisationAiService
import icu.windea.pls.ai.settings.PlsAiSettings

object PlsAiFacade {
    fun getSettings() = service<PlsAiSettings>()

    fun isEnabled() = getSettings().state.enable

    fun getTranslateLocalisationService() = service<TranslateLocalisationAiService>()

    fun getPolishLocalisationService() = service<PolishLocalisationAiService>()
}
