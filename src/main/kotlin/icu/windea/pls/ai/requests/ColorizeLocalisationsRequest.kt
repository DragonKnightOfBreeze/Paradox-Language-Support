package icu.windea.pls.ai.requests

import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import org.apache.tools.ant.*

class ColorizeLocalisationsRequest(
    val localisations: List<ParadoxLocalisationProperty>,
    val sourceLocale: ParadoxLocalisationLocale?,
    val description: String?,
    val file: PsiFile?,
    val project: Project
) : PlsAiRequest {
    override val fileInfo by lazy { selectFile(file ?: localisations.firstOrNull())?.fileInfo }
}
