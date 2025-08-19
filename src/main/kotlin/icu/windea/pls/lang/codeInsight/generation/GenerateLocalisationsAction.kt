package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.codeInsight.generation.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.script.psi.*

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
        if (file !is ParadoxScriptFile && file !is ParadoxLocalisationFile) return
        if (file.fileInfo == null) return
        presentation.isVisible = true
        val context = getContext(file, editor)
        handler.context = context
        val isEnabled = context != null
        presentation.isEnabled = isEnabled
    }

    private fun getContext(file: PsiFile, editor: Editor): ParadoxLocalisationCodeInsightContext? {
        when (file) {
            is ParadoxScriptFile -> {
                val locales = ParadoxLocaleManager.getLocaleConfigs()
                val element = findElement(file, editor.caretModel.offset)
                val contextElement = when {
                    element == null -> null
                    element.isDefinitionRootKeyOrName() -> element.findParentDefinition()
                    else -> element
                }
                if (contextElement == null) return null
                val context = when {
                    contextElement is ParadoxScriptDefinitionElement -> ParadoxLocalisationCodeInsightContextBuilder.fromDefinition(contextElement, locales)
                    contextElement is ParadoxScriptStringExpressionElement -> ParadoxLocalisationCodeInsightContextBuilder.fromExpression(contextElement, locales)
                    else -> null
                }
                return context
            }
            is ParadoxLocalisationFile -> {
                val locales = ParadoxLocaleManager.getLocaleConfigs()
                val element = findElement(file, editor.caretModel.offset)?.takeIf { it.type != null }
                val contextElement = element
                if (contextElement == null) return null
                val context = ParadoxLocalisationCodeInsightContextBuilder.fromLocalisation(contextElement, locales)
                return context
            }
            else -> return null
        }
    }

    private fun findElement(file: ParadoxScriptFile, offset: Int): ParadoxScriptExpressionElement? {
        return ParadoxPsiManager.findScriptExpression(file, offset)
    }

    private fun findElement(file: ParadoxLocalisationFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiManager.findLocalisation(file, offset)
    }

    override fun createEditTemplateAction(dataContext: DataContext?): AnAction? {
        return null
    }
}
