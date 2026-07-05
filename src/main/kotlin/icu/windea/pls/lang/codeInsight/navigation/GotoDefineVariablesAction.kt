package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 导航到当前定值变量的包括自身在内的拥有相同命名空间和名称的定值变量。
 */
class GotoDefineVariablesAction : BaseCodeInsightAction() {
    private val handler = GotoDefineVariablesHandler()

    override fun getHandler() = handler

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return // 忽略直接位于游戏或模组的根目录下的文件
        if (!ParadoxPsiFileMatchService.isScriptFile(file, ParadoxPathConstraint.ForDefine)) return // 仅限有效的脚本文件
        val element = findElement(file, editor.caretModel.offset) ?: return
        if (!ParadoxPsiMatchService.isDefineVariable(element)) return
        e.presentation.isEnabledAndVisible = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        return ParadoxPsiFileService.findScriptProperty(file, offset)
    }
}
