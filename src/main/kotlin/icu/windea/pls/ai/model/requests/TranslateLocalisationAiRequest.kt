package icu.windea.pls.ai.model.requests

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.util.manipulators.*
import java.util.concurrent.atomic.*

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
}
