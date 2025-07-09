package icu.windea.pls.tools.actions.localisation.ai

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.tools.actions.localisation.*

class ReplaceLocalisationWithAiTranslationAction : ManipulateLocalisationActionBase.WithLocalePopupAndPopup<String>() {
    override fun isAvailable(e: AnActionEvent, project: Project): Boolean {
        return super.isAvailable(e, project) && PlsAiManager.isAvailable()
    }

    override fun createPopup(e: AnActionEvent, project: Project, callback: (String) -> Unit): JBPopup? {
        return PlsAiManager.getTranslateLocalisationService().createDescriptionPopup(project, callback)
    }

    override suspend fun doHandleAll(e: AnActionEvent, project: Project, files: List<PsiFile>, selectedLocale: CwtLocaleConfig, data: String?) {
        TODO("Not yet implemented")
    }

    private suspend fun handleText(request: PlsAiTranslateLocalisationsRequest, callback: suspend (ParadoxLocalisationResult) -> Unit) {
        ParadoxLocalisationManipulator.handleTextWithAiTranslation(request, callback)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.ai.translate.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createSuccessNotification(project: Project, selectedLocale: CwtLocaleConfig, processed: Int) {
        val content = PlsBundle.message("action.replaceLocalisationWithAiTranslation.notification.0", selectedLocale, processed)
        createNotification(content, NotificationType.INFORMATION).notify(project)
    }

    private fun createSuccessWithWarningsNotification(project: Project, selectedLocale: CwtLocaleConfig, processed: Int) {
        val content = PlsBundle.message("action.replaceLocalisationWithAiTranslation.notification.2", selectedLocale, processed)
        createNotification(content, NotificationType.WARNING).notify(project)
    }

    private fun createFailedNotification(project: Project, selectedLocale: CwtLocaleConfig, processed: Int, error: Throwable) {
        thisLogger().warn(error)

        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("action.replaceLocalisationWithAiTranslation.notification.1", selectedLocale, processed) + errorDetails
        createNotification(content, NotificationType.WARNING).notify(project)
    }
}
