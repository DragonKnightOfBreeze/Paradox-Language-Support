package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 导航到当前内联脚本用法的对应的（即同名）内联脚本。
 */
class GotoInlineScriptsAction : BaseCodeInsightAction() {
    private val handler = GotoInlineScriptsHandler()

    override fun getHandler() = handler

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return // 忽略直接位于游戏或模组的根目录下的文件
        if (!ParadoxPsiFileMatchService.isScriptFile(file, ParadoxPathConstraint.AcceptInlineScriptUsage)) return // 仅限有效的脚本文件
        if (!ParadoxPsiFileMatchService.isInlineScriptSupported(file)) return // 忽略游戏类型不支持的情况
        val gameType = selectGameType(file) ?: return
        val element = findElement(file, editor.caretModel.offset) ?: return // 只要向上能找到符合条件的属性就行
        if (!ParadoxPsiMatchService.isInlineScriptUsage(element, gameType)) return
        e.presentation.isEnabledAndVisible = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        return ParadoxPsiFileService.findScriptProperty(file, offset)
    }
}

