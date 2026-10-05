package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager

/**
 * 导航到当前定义的相关注入。
 */
class GotoRelatedDefinitionInjectionsAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedDefinitionInjectionsHandler()

    override fun getHandler() = handler

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        val visible = isVisible(file)
        e.presentation.isVisible = visible
        val enabled = visible && isEnabled(file, editor)
        e.presentation.isEnabled = enabled
    }

    private fun isVisible(file: PsiFile): Boolean {
        // 忽略直接位于游戏或模组的根目录下的文件
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return false
        // 忽略游戏类型不支持的情况
        if (!ParadoxPsiFileMatchService.isDefinitionInjectionSupported(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    private fun isEnabled(file: PsiFile, editor: Editor): Boolean {
        val offset = editor.caretModel.offset
        val element = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return false
        if (!element.isDefinitionTypeKeyOrName()) return false
        val definition = selectScope { element.parentDefinition() } ?: return false
        val definitionInfo = definition.definitionInfo ?: return false
        // 排除不期望匹配的定义
        return ParadoxDefinitionInjectionManager.canApply(definitionInfo)
    }

}
