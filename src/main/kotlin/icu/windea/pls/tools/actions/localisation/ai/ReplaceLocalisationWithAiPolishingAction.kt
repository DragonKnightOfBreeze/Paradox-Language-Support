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
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.tools.actions.localisation.*

class ReplaceLocalisationWithAiPolishingAction : ManipulateLocalisationActionBase.WithPopup<String>() {
    override fun isAvailable(e: AnActionEvent, project: Project): Boolean {
        return super.isAvailable(e, project) && PlsAiManager.isAvailable()
    }

    override fun createPopup(e: AnActionEvent, project: Project, callback: (String) -> Unit): JBPopup? {
        return PlsAiManager.getPolishLocalisationService().createDescriptionPopup(project, callback)
    }

    override suspend fun doHandleAll(e: AnActionEvent, project: Project, context: Context<String>) {
        TODO("Not yet implemented")
    }

    private suspend fun handleText(request: PlsAiPolishLocalisationsRequest, callback: suspend (ParadoxLocalisationResult) -> Unit) {
        ParadoxLocalisationManipulator.handleTextWithAiPolishing(request, callback)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.ai.polish.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createSuccessNotification(project: Project, processed: Int) {
        val content = PlsBundle.message("action.replaceLocalisationWithAiPolishing.notification.0", processed)
        createNotification(content, NotificationType.INFORMATION).notify(project)
    }

    private fun createSuccessWithWarningsNotification(project: Project, processed: Int) {
        val content = PlsBundle.message("action.replaceLocalisationWithAiPolishing.notification.2", processed)
        createNotification(content, NotificationType.WARNING).notify(project)
    }

    private fun createFailedNotification(project: Project, processed: Int, error: Throwable) {
        thisLogger().warn(error)

        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("action.replaceLocalisationWithAiPolishing.notification.1", processed) + errorDetails
        createNotification(content, NotificationType.WARNING).notify(project)
    }
}
