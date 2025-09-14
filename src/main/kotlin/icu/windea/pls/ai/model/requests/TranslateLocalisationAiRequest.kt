package icu.windea.pls.ai.model.requests

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import java.util.concurrent.atomic.AtomicInteger

@Suppress("unused")
class TranslateLocalisationAiRequest(
    project: Project,
    file: PsiFile,
    localisationContexts: List<ParadoxLocalisationContext>,
    val targetLocale: CwtLocaleConfig,
    val description: String?
) : ManipulateLocalisationAiRequest(project, file, localisationContexts) {
    companion object {
        private val idIncrementer = AtomicInteger()
    }

    override val requestId by lazy { idIncrementer.getAndIncrement().toString() }

    override fun toPromptVariables(variables: MutableMap<String, Any?>): Map<String, Any?> {
        super.toPromptVariables(variables)
        variables["target_locale_id"] = targetLocale.id
        variables["target_locale_text"] = targetLocale.text
        variables["description"] = description
        return variables
    }
}
