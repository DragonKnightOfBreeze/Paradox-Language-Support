package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.isDefinitionTypeKeyOrName

/**
 * 导航到当前定义的相关注入。
 */
class GotoRelatedDefinitionInjectionsAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedDefinitionInjectionsHandler()

    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (!ParadoxPsiFileMatcher.isScriptFile(file, injectable = true)) return
        if (ParadoxPsiFileMatcher.isTopFile(file)) return // 忽略直接位于游戏或模组目录（或者对应的入口目录）中的文件
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
        return ParadoxPsiFileManager.findScriptExpression(file, offset).castOrNull()
    }
}
