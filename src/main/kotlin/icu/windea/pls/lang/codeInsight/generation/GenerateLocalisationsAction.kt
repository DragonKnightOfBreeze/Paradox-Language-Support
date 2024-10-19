package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.codeInsight.generation.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.script.psi.*

/**
 * 生成当前定义的所有（缺失的）本地化。
 */
class GenerateLocalisationsAction : BaseCodeInsightAction(), GenerateActionPopupTemplateInjector {
    private val handler = ParadoxGenerateLocalisationsHandler()

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
        if (file.fileInfo == null) return
        presentation.isVisible = true
        if (file.definitionInfo != null) {
            presentation.isEnabled = true
            return
        }
        val context = getContext(file, editor)
        handler.context = context
        val isEnabled = context != null
        presentation.isEnabled = isEnabled
    }

    private fun getContext(file: PsiFile, editor: Editor): ParadoxLocalisationCodeInsightContext? {
        val locales = ParadoxLocaleManager.getLocaleConfigs()
        val element = findElement(file, editor.caretModel.offset)
        val contextElement = when {
            element == null -> null
            element.isDefinitionRootKeyOrName() -> element.findParentDefinition()
            else -> element
        }
        val context = when {
            contextElement == null -> null
            contextElement is ParadoxScriptDefinitionElement -> ParadoxLocalisationCodeInsightContext.fromDefinition(contextElement, locales)
            contextElement is ParadoxScriptStringExpressionElement -> ParadoxLocalisationCodeInsightContext.fromExpression(contextElement, locales)
            else -> null
        }
        return context
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        return ParadoxPsiManager.findScriptExpression(file, offset).castOrNull()
    }

    override fun createEditTemplateAction(dataContext: DataContext?): AnAction? {
        return null
    }
}
