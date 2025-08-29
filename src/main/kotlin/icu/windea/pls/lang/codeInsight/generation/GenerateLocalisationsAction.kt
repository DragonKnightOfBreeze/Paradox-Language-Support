package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.codeInsight.generation.actions.GenerateActionPopupTemplateInjector
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxPsiManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.model.codeInsight.ParadoxLocalisationCodeInsightContextBuilder
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.findParentDefinition
import icu.windea.pls.script.psi.isDefinitionRootKeyOrName

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
