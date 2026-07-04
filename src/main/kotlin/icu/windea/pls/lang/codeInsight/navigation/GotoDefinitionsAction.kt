package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.editor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 导航到当前定义的包括自身在内的拥有相同名称和主要类型的定义。
 *
 * 不支持直接声明为文件的定义。
 */
class GotoDefinitionsAction : BaseCodeInsightAction() {
    private val handler = GotoDefinitionsHandler()

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
        if (!element.isDefinitionTypeKeyOrName()) return
        e.presentation.isEnabled = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
        return ParadoxPsiFileService.findScriptExpression(file, offset).castOrNull()
    }
}
