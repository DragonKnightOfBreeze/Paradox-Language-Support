package icu.windea.pls.script.refactoring

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.*

/**
 * 声明全局封装变量的重构。
 */
object ParadoxScriptIntroduceGlobalScriptedVariableHandler: RefactoringActionHandler {
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {
		//TODO
	}
	
	override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
		//not support
	}
}