package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 导航到当前封装变量的包括自身在内的相同名称的封装变量（仅限本地+全局）。
 */
class GotoScriptedVariablesAction : BaseCodeInsightAction() {
    private val handler = GotoScriptedVariablesHandler()

    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (!ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = true)) return
        if (ParadoxPsiFileMatcher.isTopFile(file)) return // 忽略直接位于游戏或模组目录（或者对应的入口目录）中的文件
        presentation.isVisible = true
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        if (!ParadoxPsiMatcher.isScriptedVariable(element)) return
        presentation.isEnabled = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptScriptedVariable? {
        return ParadoxPsiFileManager.findScriptedVariable(file, offset) { BY_NAME }
    }
}
