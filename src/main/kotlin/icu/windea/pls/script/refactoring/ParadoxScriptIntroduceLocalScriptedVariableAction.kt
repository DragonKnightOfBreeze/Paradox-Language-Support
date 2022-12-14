package icu.windea.pls.script.refactoring

import com.intellij.lang.refactoring.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import com.intellij.refactoring.*
import com.intellij.refactoring.actions.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 声明本地封装变量的动作。
 */
class ParadoxScriptIntroduceLocalScriptedVariableAction : BasePlatformRefactoringAction() {
	init {
		addTextOverride(ActionPlaces.MAIN_MENU, PlsBundle.message("action.ParadoxScript.IntroduceGlobalScriptedVariable.text.mainMenu"))
	}
	
	val handler = ParadoxScriptIntroduceLocalScriptedVariableHandler()
	
	override fun isAvailableInEditorOnly(): Boolean {
		return true
	}
	
	override fun isEnabledOnElements(elements: Array<out PsiElement>): Boolean {
		return false
	}
	
	override fun isAvailableForFile(file: PsiFile): Boolean {
		return file is ParadoxScriptFile
	}
	
	override fun getRefactoringHandler(provider: RefactoringSupportProvider): RefactoringActionHandler? {
		return handler
	}
}