package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.withErrorRef
import icu.windea.pls.ide.notification.PlsNotificationGroups
import icu.windea.pls.lang.manipulators.ParadoxLocalisationManipulationContext
import icu.windea.pls.lang.manipulators.ParadoxLocalisationManipulationContextBuilder
import icu.windea.pls.lang.manipulators.ParadoxLocalisationManipulator
import java.awt.datatransfer.StringSelection
import java.util.concurrent.atomic.AtomicReference

/**
 * 复制来自特定语言环境的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationFromLocaleIntention : ManipulateLocalisationIntentionBase.WithLocalePopup() {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationFromLocale")

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile, context: Context) {
        val (elements, selectedLocale) = context
        withBackgroundProgress(project, PlsBundle.message("intention.copyLocalisationFromLocale.progress.title", selectedLocale.text)) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationManipulationContextBuilder.from(it) }.toList() }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()

            runCatchingCancelable r@{
                if (contextsToHandle.isEmpty()) return@r
                reportProgressScope(contextsToHandle.size) { reporter ->
                    contextsToHandle.forEachConcurrent f@{ context ->
                        reporter.itemStep(PlsBundle.message("manipulation.localisation.search.progress.itemStep", context.key)) {
                            withErrorRef(errorRef) { handleText(context, project, selectedLocale) }.getOrThrow()
                        }
                    }
                }
            }

            if (errorRef.get() == null) {
                val textToCopy = ParadoxLocalisationManipulator.joinText(contexts)
                CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
            }
            createNotification(selectedLocale, errorRef.get()).notify(project)
        }
    }

    private suspend fun handleText(context: ParadoxLocalisationManipulationContext, project: Project, selectedLocale: CwtLocaleConfig) {
         ParadoxLocalisationManipulator.searchTextFromLocale(context, project, selectedLocale)
    }

    private fun createNotification(selectedLocale: CwtLocaleConfig, error: Throwable?): Notification {
        if (error == null) {
            val content = PlsBundle.message("intention.copyLocalisationFromLocale.notification", selectedLocale.text, Messages.success())
            return PlsNotificationGroups.manipulation().createNotification(content, NotificationType.INFORMATION)
        }

        thisLogger().warn(error)
        val errorDetails = error.message?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("intention.copyLocalisationFromLocale.notification", selectedLocale.text, Messages.failed()) + errorDetails
        return PlsNotificationGroups.manipulation().createNotification(content, NotificationType.WARNING)
    }
}
