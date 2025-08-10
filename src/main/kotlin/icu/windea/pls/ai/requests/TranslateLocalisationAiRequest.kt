package icu.windea.pls.ai.requests

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.util.manipulators.*

class TranslateLocalisationAiRequest(
    project: Project,
    file: PsiFile,
    localisationContexts: List<ParadoxLocalisationContext>,
    val targetLocale: CwtLocaleConfig,
    val description: String?
) : ManipulateLocalisationAiRequest(project, file, localisationContexts)
