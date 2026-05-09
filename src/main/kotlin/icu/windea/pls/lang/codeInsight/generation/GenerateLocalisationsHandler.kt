package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContextBuilder
import icu.windea.pls.lang.ui.ParadoxLocaleListPopup
import icu.windea.pls.lang.util.ParadoxLocaleManager

class GenerateLocalisationsHandler(
    var context: ParadoxLocalisationCodeInsightContext? = null,
    val fromInspection: Boolean = false,
) : CodeInsightActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val allLocales = ParadoxLocaleManager.getLocaleConfigs()
        val localePopup = ParadoxLocaleListPopup(allLocales)
        localePopup.onSelected { selectedValue ->
            ParadoxLocalisationGenerationManager.handleGeneration(file, editor, context, selectedValue)
        }
        JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
    }

    fun updateContext(file: PsiFile, editor: Editor) {
        val locales = ParadoxLocaleManager.getLocaleConfigs()
        context = ParadoxLocalisationCodeInsightContextBuilder.fromContextElement(file, editor, locales)
    }
}
