package icu.windea.pls.ai.requests

import com.intellij.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import org.apache.tools.ant.*

class TranslateLocalisationsRequest(
    val localisations: List<ParadoxLocalisationProperty>,
    val text: String,
    val sourceLocale: ParadoxLocalisationLocale?,
    val targetLocale: ParadoxLocalisationLocale,
    val file: PsiFile?,
    val project: Project
) : PlsAiRequest {
    override val fileInfo by lazy { selectFile(file ?: localisations.firstOrNull())?.fileInfo }
}
