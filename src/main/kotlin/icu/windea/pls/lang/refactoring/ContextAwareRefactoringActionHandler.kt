package icu.windea.pls.lang.refactoring

import com.intellij.lang.ContextAwareActionHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.RefactoringBundle
import com.intellij.refactoring.util.CommonRefactoringUtil

abstract class ContextAwareRefactoringActionHandler : RefactoringActionHandler, ContextAwareActionHandler {
    abstract fun isAvailable(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean

    abstract fun invokeAction(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Boolean

    final override fun isAvailableForQuickList(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
        return isAvailable(editor, file, dataContext)
    }

    final override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext) {
        val result = invokeAction(project, editor, file, dataContext)
        if (!result) {
            CommonRefactoringUtil.showErrorHint(project, editor, RefactoringBundle.message("refactoring.introduce.context.error"), "ERROR", null)
        }
    }

    final override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        //not support
    }
}
