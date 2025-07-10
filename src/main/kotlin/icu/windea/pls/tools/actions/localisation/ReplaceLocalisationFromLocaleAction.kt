package icu.windea.pls.tools.actions.localisation

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*

class ReplaceLocalisationFromLocaleAction : ManipulateLocalisationActionBase.WithLocalePopup() {
    override suspend fun doHandleAll(e: AnActionEvent, project: Project, context: Context) {
        TODO("Not yet implemented")
    }

    private suspend fun handleText(context: ParadoxLocalisationContext, project: Project, selectedLocale: CwtLocaleConfig) {
        return ParadoxLocalisationManipulator.handleTextFromLocale(context, project, selectedLocale)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createSuccessNotification(project: Project, selectedLocale: CwtLocaleConfig, processed: Int) {
        val content = PlsBundle.message("action.replaceLocalisationFromLocale.notification.0", selectedLocale, processed)
        createNotification(content, NotificationType.INFORMATION).notify(project)
    }

    private fun createFailedNotification(project: Project, selectedLocale: CwtLocaleConfig, processed: Int, error: Throwable) {
        thisLogger().warn(error)

        val errorDetails = error.message?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("action.replaceLocalisationFromLocale.notification.1", selectedLocale, processed) + errorDetails
        createNotification(content, NotificationType.WARNING).notify(project)
    }
}
