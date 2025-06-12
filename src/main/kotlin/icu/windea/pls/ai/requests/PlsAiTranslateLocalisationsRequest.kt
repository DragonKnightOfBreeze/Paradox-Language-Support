package icu.windea.pls.ai.requests

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*

class PlsAiTranslateLocalisationsRequest(
    val localisations: List<ParadoxLocalisationProperty>,
    val text: String,
    val description: String?,
    val targetLocale: CwtLocaleConfig,
    val file: PsiFile?,
    val project: Project
) : PlsAiRequest {
    override val fileInfo by lazy { selectFile(file ?: localisations.firstOrNull())?.fileInfo }
}
