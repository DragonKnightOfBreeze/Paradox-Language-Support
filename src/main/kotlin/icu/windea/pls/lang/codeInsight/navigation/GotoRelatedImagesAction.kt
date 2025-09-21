package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isDefinitionRootKeyOrName

/**
 * 导航到当前目标的相关图片的动作。
 *
 * 支持的目标：
 * - 定义（来自顶级键或名字）
 * - 修正（来自对应的脚本表达式）
 */
class GotoRelatedImagesAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedImagesHandler()

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
            element.isDefinitionRootKeyOrName() -> true
            element is ParadoxScriptStringExpressionElement -> ParadoxModifierManager.resolveModifier(element) != null
            else -> false
        }
        presentation.isEnabled = isEnabled
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
        return ParadoxPsiFinder.findScriptExpression(file, offset).castOrNull()
    }
}
