package icu.windea.pls.ai

import com.intellij.openapi.components.*
import icu.windea.pls.ai.services.*
import icu.windea.pls.ai.settings.*
import icu.windea.pls.ai.util.*

object PlsAiFacade {
    fun getSettings(): PlsAiSettingsState = service<PlsAiSettings>().state

    fun isAvailable(): Boolean = getSettings().enable && PlsChatModelManager.isAvailable()

    fun getTranslateLocalisationService() = service<TranslateLocalisationAiService>()

    fun getPolishLocalisationService() = service<PolishLocalisationAiService>()
}
