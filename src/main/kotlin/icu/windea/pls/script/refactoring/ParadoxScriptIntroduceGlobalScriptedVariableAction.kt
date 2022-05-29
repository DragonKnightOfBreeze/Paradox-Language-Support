package icu.windea.pls.script.refactoring

import com.intellij.lang.refactoring.*
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.psi.*
import com.intellij.refactoring.*
import com.intellij.refactoring.actions.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 声明全局封装变量的动作。
 */
class ParadoxScriptIntroduceGlobalScriptedVariableAction: BasePlatformRefactoringAction(){
	init {
		addTextOverride(ActionPlaces.MAIN_MENU, PlsBundle.message("action.ParadoxScript.IntroduceLocalScriptedVariable.text.mainMenu"))
	}
	
	override fun isAvailableInEditorOnly(): Boolean {
		return true
	}
	
	override fun isAvailableForFile(file: PsiFile): Boolean {
		return file is ParadoxScriptFile
	}
	
	override fun getRefactoringHandler(provider: RefactoringSupportProvider): RefactoringActionHandler? {
		return ParadoxScriptIntroduceGlobalScriptedVariableHandler
	}
}