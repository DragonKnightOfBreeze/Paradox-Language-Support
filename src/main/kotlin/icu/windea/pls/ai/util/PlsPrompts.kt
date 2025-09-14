package icu.windea.pls.ai.util

import icu.windea.pls.ai.model.requests.PolishLocalisationAiRequest
import icu.windea.pls.ai.model.requests.TranslateLocalisationAiRequest
import icu.windea.pls.ai.prompts.PromptManager
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext

object PlsPrompts {
    fun translateLocalisation(request: TranslateLocalisationAiRequest): String {
        return PromptManager.fromTemplate("translate-localisation", request)
    }

    fun polishLocalisation(request: PolishLocalisationAiRequest): String {
        return PromptManager.fromTemplate("polish-localisation", request)
    }

    fun fromLocalisationContexts(localisationContexts: List<ParadoxLocalisationContext>): String {
        return localisationContexts.joinToString("\n") { context -> "${context.key}: \"${context.newText}\"" }.trim()
    }
}
