package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxPsiFinder
import icu.windea.pls.lang.psi.findParentDefinition
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.isDefinitionTypeKeyOrName

/**
 * 导航到当前定义的对应的定义注入。
 *
 * 不支持直接声明为文件的定义。
 */
class GotoDefinitionInjectionsAction : BaseCodeInsightAction() {
    private val handler = GotoDefinitionInjectionsHandler()

    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file !is ParadoxScriptFile) return
        val fileInfo = file.fileInfo ?: return
        if (fileInfo.path.length <= 1) return // 忽略直接位于游戏或模组入口目录下的文件
        presentation.isVisible = true
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        if (!element.isDefinitionTypeKeyOrName()) return
        val definition = element.findParentDefinition() ?: return
        val definitionInfo = definition.definitionInfo ?: return
        if (definitionInfo.name.isEmpty()) return // 排除匿名定义
        presentation.isEnabled = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
        return ParadoxPsiFinder.findScriptExpression(file, offset).castOrNull()
    }
}
