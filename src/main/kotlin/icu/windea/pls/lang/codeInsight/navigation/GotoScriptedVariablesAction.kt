package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 导航到当前封装变量的包括自身在内的拥有相同名称的封装变量（仅限本地+全局）。
 */
class GotoScriptedVariablesAction : BaseCodeInsightAction() {
    private val handler = GotoScriptedVariablesHandler()

    override fun getHandler() = handler

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return // 忽略直接位于游戏或模组的根目录下的文件
        if (!ParadoxPsiFileMatchService.isScriptFile(file)) return // 仅限有效的脚本文件
        e.presentation.isVisible = true
        val element = findElement(file, editor.caretModel.offset) ?: return
        if (!ParadoxPsiMatchService.isScriptedVariable(element)) return
        e.presentation.isEnabled = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptScriptedVariable? {
        return ParadoxPsiFileService.findScriptedVariable(file, offset) { BY_NAME }
    }
}
