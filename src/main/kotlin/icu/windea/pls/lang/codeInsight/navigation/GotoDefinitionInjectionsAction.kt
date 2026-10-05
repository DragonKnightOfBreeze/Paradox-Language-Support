package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.model.constraints.ParadoxPathConstraint

/**
 * 导航到当前定义注入的同目标的所有定义注入。
 */
class GotoDefinitionInjectionsAction : BaseCodeInsightAction() {
    private val handler = GotoDefinitionInjectionsHandler()

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
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求受游戏类型支持
        if (!ParadoxPsiFileMatchService.isDefinitionInjectionSupported(file)) return false
        // 要求是语义上有效的脚本文件（可接受定义注入）
        return ParadoxPsiFileMatchService.isScriptFile(file, ParadoxPathConstraint.AcceptDefinitionInjection)
    }

    private fun isEnabled(file: PsiFile, editor: Editor): Boolean {
        val offset = editor.caretModel.offset
        // 只要向上能找到符合条件的属性就行
        val element = ParadoxPsiFileService.findScriptProperty(file, offset) ?: return false
        val info = element.definitionInjectionInfo ?: return false
        // 排除目标或目标类型为空的情况
        return info.isTargetValid()
    }
}
