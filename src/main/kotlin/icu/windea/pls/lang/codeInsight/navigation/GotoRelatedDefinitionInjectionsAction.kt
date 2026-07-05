package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.editor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

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
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return // 忽略直接位于游戏或模组的根目录下的文件
        if (!ParadoxPsiFileMatchService.isScriptFile(file)) return // 仅限有效的脚本文件
        if (!ParadoxPsiFileMatchService.isDefinitionInjectionSupported(file)) return // 忽略游戏类型不支持的情况
        e.presentation.isVisible = true
        val element = findElement(file, editor.caretModel.offset) ?: return
        if (!element.isDefinitionTypeKeyOrName()) return
        val definition = selectScope { element.parentDefinition() } ?: return
        val definitionInfo = definition.definitionInfo ?: return
        if (!ParadoxDefinitionInjectionManager.canApply(definitionInfo)) return // 排除不期望匹配的定义
        e.presentation.isEnabled = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
        return ParadoxPsiFileService.findScriptExpression(file, offset).castOrNull()
    }
}
