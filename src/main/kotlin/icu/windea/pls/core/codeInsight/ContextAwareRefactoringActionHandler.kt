package icu.windea.pls.core.codeInsight

import com.intellij.lang.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.*
import com.intellij.refactoring.util.*

abstract class ContextAwareRefactoringActionHandler : RefactoringActionHandler, ContextAwareActionHandler {
	abstract fun isAvailable(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean
	
	abstract fun invokeAction(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Boolean
	
	final override fun isAvailableForQuickList(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		return isAvailable(editor, file, dataContext)
	}
	
	final override fun invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext) {
		val result = invokeAction(project, editor, file, dataContext)
		if(!result) {
			CommonRefactoringUtil.showErrorHint(project, editor, RefactoringBundle.message("refactoring.introduce.context.error"), "Error", null)
		}
	}
	
	final override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
		//not support
	}
}