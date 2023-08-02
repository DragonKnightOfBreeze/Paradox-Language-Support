package icu.windea.pls.core.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.hint.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.script.inspections.general.*
import icu.windea.pls.script.psi.*

class ParadoxGenerateLocalisationsHandler(
    private val context: ParadoxLocalisationCodeInsightContext? = null,
    private val inspection: MissingLocalisationInspection? = null,
    private val forFile: Boolean = false,
) : CodeInsightActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val context = getContext(file, editor)
        if(context == null) {
            HintManager.getInstance().showErrorHint(editor, PlsBundle.message("generation.localisation.noMembersHint"))
            return
        }
        val members = ParadoxLocalisationGenerator.getMembers(context)
        if(members.isEmpty()) {
            HintManager.getInstance().showErrorHint(editor, PlsBundle.message("generation.localisation.noMembersHint"))
            return
        }
        val chooser = ParadoxLocalisationGenerator.showChooser(context, members, project) ?: return
        val selectedElements = chooser.selectedElements ?: return
        if(selectedElements.isEmpty()) return
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        ParadoxLocalisationGenerator.generate(context, selectedElements, project, file)
    }
    
    private fun getContext(file: PsiFile, editor: Editor, inspection: MissingLocalisationInspection? = null): ParadoxLocalisationCodeInsightContext? {
        if(context != null) return context
        val locales = ParadoxLocaleHandler.getLocaleConfigs()
        if(forFile) return ParadoxLocalisationCodeInsightContext.fromFile(file, locales, inspection)
        val element = findElement(file, editor.caretModel.offset)
        val contextElement = when {
            element == null -> null
            element.isDefinitionRootKeyOrName() -> element.findParentDefinition()
            ParadoxModifierHandler.resolveModifier(element) != null -> element
            else -> null
        }
        val context = when {
            contextElement == null -> null
            contextElement is ParadoxScriptDefinitionElement -> ParadoxLocalisationCodeInsightContext.fromDefinition(contextElement, locales, inspection)
            contextElement is ParadoxScriptStringExpressionElement -> ParadoxLocalisationCodeInsightContext.fromExpression(contextElement, locales, inspection)
            else -> null
        }
        return context
    }
    
    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        return ParadoxPsiFinder.findScriptExpression(file, offset).castOrNull()
    }
}
