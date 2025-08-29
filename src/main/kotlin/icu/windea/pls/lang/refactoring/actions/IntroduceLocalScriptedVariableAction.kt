package icu.windea.pls.lang.refactoring.actions

import com.intellij.lang.refactoring.RefactoringSupportProvider
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.actions.BasePlatformRefactoringAction
import icu.windea.pls.PlsBundle
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 声明本地封装变量的动作。
 */
class IntroduceLocalScriptedVariableAction : BasePlatformRefactoringAction() {
    init {
        addTextOverride(ActionPlaces.MAIN_MENU, PlsBundle.message("action.Pls.Script.IntroduceGlobalScriptedVariable.text.mainMenu"))
    }

    val handler = IntroduceLocalScriptedVariableHandler()

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
