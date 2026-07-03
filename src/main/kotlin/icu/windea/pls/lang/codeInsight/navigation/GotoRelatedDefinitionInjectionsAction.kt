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
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 导航到当前定义的相关注入。
 */
class GotoRelatedDefinitionInjectionsAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedDefinitionInjectionsHandler()

    override fun getHandler() = handler

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (ParadoxPsiFileMatchService.isTopFileFromRoot(file)) return // 忽略直接位于游戏或模组的根目录下的文件
        if (!ParadoxPsiFileMatchService.isScriptFile(file, injectable = true)) return
        val gameType = selectGameType(file)
        if (!ParadoxDefinitionInjectionManager.isSupported(gameType)) return // 忽略游戏类型不支持的情况
        presentation.isVisible = true
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        if (!element.isDefinitionTypeKeyOrName()) return
        val definition = selectScope { element.parentDefinition() } ?: return
        val definitionInfo = definition.definitionInfo ?: return
        if (!ParadoxDefinitionInjectionManager.canApply(definitionInfo)) return // 排除不期望匹配的定义
        presentation.isEnabled = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
        return ParadoxPsiFileService.findScriptExpression(file, offset).castOrNull()
    }
}
