package icu.windea.pls.ai.requests

import com.intellij.openapi.project.Project
import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*

class PlsAiPolishLocalisationsRequest(
    val localisations: List<ParadoxLocalisationProperty>,
    val text: String,
    val description: String?, //TODO 2.0.0-dev 可以自定义的润色要求
    val file: PsiFile?,
    val project: Project
) : PlsAiRequest {
    override val fileInfo by lazy { selectFile(file ?: localisations.firstOrNull())?.fileInfo }
}
