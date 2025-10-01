package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.lang.util.psi.ParadoxPsiMatcher
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDefinitionTypeKeyOrName

/**
 * 导航到当前目标的相关本地化的动作。
 *
 * 支持的目标：
 * - 封装变量（来自名字）
 * - 定义（来自类型键或名字）
 * - 修正（来自对应的脚本表达式）
 */
class GotoRelatedLocalisationsAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedLocalisationsHandler()

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
        presentation.isVisible = true
        if (file.definitionInfo != null) {
            presentation.isEnabled = true
            return
        }
        val offset = editor.caretModel.offset
        val element = findElement(file, offset)
        val isEnabled = when {
            element == null -> false
            ParadoxPsiMatcher.isScriptedVariable(element) -> true
            element !is ParadoxScriptStringExpressionElement -> false
            element.isDefinitionTypeKeyOrName() -> true
            else -> ParadoxModifierManager.resolveModifier(element) != null
        }
        presentation.isEnabled = isEnabled
    }

    private fun findElement(file: PsiFile, offset: Int): PsiElement? {
        return ParadoxPsiFinder.findScriptedVariable(file, offset) { BY_NAME }
            ?: ParadoxPsiFinder.findScriptExpression(file, offset).castOrNull()
    }
}

