package icu.windea.pls.core.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.model.codeInsight.*

class GenerateLocalisationsHandler(
    private val context: ParadoxLocalisationCodeInsightContext? = null
) : CodeInsightActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        ParadoxPsiGenerator.generateLocalisations(project, editor, file, context)
    }
}
