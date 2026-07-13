package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContext
import icu.windea.pls.lang.codeInsight.ParadoxLocalisationCodeInsightContextService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.ui.ParadoxLocaleListPopup
import icu.windea.pls.lang.util.ParadoxLocaleManager

class GenerateLocalisationsInFileHandler(
    var context: ParadoxLocalisationCodeInsightContext? = null,
    val fromInspection: Boolean = false,
) : CodeInsightActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val configGroup = ChronicleFacade.getConfigGroup(project, selectGameType(file))
        val supportedLocales = ParadoxLocaleManager.getSupportedLocales(configGroup)
        val localePopup = ParadoxLocaleListPopup(supportedLocales)
        localePopup.onSelected { selectedValue ->
            updateContext(file)
            ParadoxLocalisationGenerationManager.handleGeneration(file, editor, context, selectedValue)
        }
        JBPopupFactory.getInstance().createListPopup(localePopup).showInBestPositionFor(editor)
    }

    fun updateContext(file: PsiFile) {
        val project = file.project
        val configGroup = ChronicleFacade.getConfigGroup(project, selectGameType(file))
        val supportedLocales = ParadoxLocaleManager.getSupportedLocales(configGroup)
        context = ParadoxLocalisationCodeInsightContextService.fromFile(file, supportedLocales, fromInspection)
    }
}
