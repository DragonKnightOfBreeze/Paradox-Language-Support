package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.hint.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.ui.*
import icu.windea.pls.lang.ui.locale.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.codeInsight.*
import icu.windea.pls.script.psi.*

class ParadoxGenerateLocalisationsHandler(
    var context: ParadoxLocalisationCodeInsightContext? = null,
    val forFile: Boolean = false,
    val fromInspection: Boolean = false,
) : CodeInsightActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val onChosen = action@{ selected: CwtLocalisationLocaleConfig ->
            val context = getFinalContext(file)
            if(context == null) {
                HintManager.getInstance().showErrorHint(editor, PlsBundle.message("generation.localisation.noMembersHint"))
                return@action
            }
            val members = ParadoxLocalisationGenerator.getMembers(context, selected)
            if(members.isEmpty()) {
                HintManager.getInstance().showErrorHint(editor, PlsBundle.message("generation.localisation.noMembersHint"))
                return@action
            }
            val chooser = ParadoxLocalisationGenerator.showChooser(context, members, project) ?: return@action
            val selectedElements = chooser.selectedElements ?: return@action
            if(selectedElements.isEmpty()) return@action
            PsiDocumentManager.getInstance(project).commitAllDocuments()
            ParadoxLocalisationGenerator.generate(context, selectedElements, project, file, selected)
        }
        
        val selectedLocale = ParadoxLocaleHandler.getPreferredLocaleConfig()
        val allLocales = ParadoxLocaleHandler.getLocaleConfigs()
        val localePopup = ParadoxLocaleListPopup(selectedLocale, allLocales, onChosen)
        JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
    }
    
    private fun getFinalContext(file: PsiFile): ParadoxLocalisationCodeInsightContext? {
        if(forFile) {
            val locales = ParadoxLocaleHandler.getLocaleConfigs()
            return ParadoxLocalisationCodeInsightContext.fromFile(file, locales, fromInspection = fromInspection)
        }
        return context
    }
    
    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        return ParadoxPsiManager.findScriptExpression(file, offset).castOrNull()
    }
}
