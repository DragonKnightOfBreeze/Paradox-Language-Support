package icu.windea.pls.core.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*

class GenerateLocalisationsInFileHandler : CodeInsightActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val contextKey = PlsKeys.generateLocalisationsInFileContextKey
        val context = file.getUserData(contextKey)
            ?: getDefaultContext(project, editor, file)
            ?: return
        file.putUserData(contextKey, null)
        ParadoxGenerator.generateLocalisationsInFile(context, project, editor, file)
    }
    
    private fun getDefaultContext(project: Project, editor: Editor, file: PsiFile): GenerateLocalisationsInFileContext? {
        return null //TODO
    }
}
 