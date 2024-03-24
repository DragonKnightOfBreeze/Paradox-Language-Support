package icu.windea.pls.lang.refactoring.actions

import com.intellij.lang.refactoring.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import com.intellij.refactoring.*
import com.intellij.refactoring.actions.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 声明全局封装变量的动作。
 */
class IntroduceGlobalScriptedVariableAction: BasePlatformRefactoringAction(){
	init {
		addTextOverride(ActionPlaces.MAIN_MENU, PlsBundle.message("action.Pls.IntroduceLocalScriptedVariable.text.mainMenu"))
	}
	
	private val handler = IntroduceGlobalScriptedVariableHandler()
	
	override fun isAvailableInEditorOnly(): Boolean {
		return true
	}
	
	override fun isEnabledOnElements(elements: Array<out PsiElement>): Boolean {
		return false
	}
	
	override fun isAvailableForFile(file: PsiFile): Boolean {
		return file is ParadoxScriptFile
	}
	
	override fun getRefactoringHandler(provider: RefactoringSupportProvider): RefactoringActionHandler {
		return handler
	}
}