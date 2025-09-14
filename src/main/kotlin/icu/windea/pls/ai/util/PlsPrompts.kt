package icu.windea.pls.ai.util

import com.intellij.DynamicBundle
import icu.windea.pls.ai.model.requests.PolishLocalisationAiRequest
import icu.windea.pls.ai.model.requests.TranslateLocalisationAiRequest
import icu.windea.pls.ai.prompts.PromptTemplateEngine
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import org.intellij.lang.annotations.Language
import java.util.*

object PlsPrompts {
    private val engine = PromptTemplateEngine()

    fun translateLocalisation(request: TranslateLocalisationAiRequest): String {
        return fromTemplate("/prompts/translate-localisation_zh.md", "/prompts/translate-localisation.md", request.toPromptVariables())
    }

    fun polishLocalisation(request: PolishLocalisationAiRequest): String {
        return fromTemplate("/prompts/polish-localisation_zh.md", "/prompts/polish-localisation.md", request.toPromptVariables())
    }

    fun fromTemplate(@Language("file-reference") path: String, variables: Map<String, Any?>): String {
        return engine.render(path, variables)
    }

    fun fromTemplate(@Language("file-reference") p1: String, @Language("file-reference") p2: String, variables: Map<String, Any?>): String {
        val path = when (DynamicBundle.getLocale()) {
            Locale.SIMPLIFIED_CHINESE -> p1
            else -> p2
        }
        return fromTemplate(path, variables)
    }

    fun fromLocalisationContexts(localisationContexts: List<ParadoxLocalisationContext>): String {
        return localisationContexts.joinToString("\n") { context -> "${context.key}: \"${context.newText}\"" }.trim()
    }
}
