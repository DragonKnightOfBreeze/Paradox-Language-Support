package icu.windea.pls.core.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*

class GenerateLocalisationsHandler : CodeInsightActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val contextKey = PlsKeys.generateLocalisationsContextKey
        val context = file.getUserData(contextKey)
            ?: getDefaultContext(project, editor, file)
            ?: return
        file.putUserData(contextKey, null)
        ParadoxGenerator.generateLocalisations(context)
    }
    
    private fun getDefaultContext(project: Project, editor: Editor, file: PsiFile): GenerateLocalisationsContext? {
        return null //TODO
    }
}

