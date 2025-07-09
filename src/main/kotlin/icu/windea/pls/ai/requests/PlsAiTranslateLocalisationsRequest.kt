package icu.windea.pls.ai.requests

import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.localisation.psi.*

class PlsAiTranslateLocalisationsRequest(
    val inputContexts: List<ParadoxLocalisationContext>,
    val text: String,
    val description: String?,
    val targetLocale: CwtLocaleConfig,
    val file: PsiFile?,
    val project: Project
) : PlsAiRequest {
    var index: Int = 0

    override val fileInfo by lazy { selectFile(file ?: inputContexts.firstOrNull()?.element)?.fileInfo }
}
