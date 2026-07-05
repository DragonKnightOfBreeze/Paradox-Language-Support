package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.withErrorRef
import icu.windea.pls.ide.notification.ChronicleNotificationGroups
import icu.windea.pls.integrations.translation.TranslationToolService
import icu.windea.pls.lang.manipulation.ParadoxLocalisationManipulationContext
import icu.windea.pls.lang.manipulation.ParadoxLocalisationManipulationService
import icu.windea.pls.lang.selectLocale
import java.util.concurrent.atomic.AtomicReference

/**
 * 替换为翻译后来自指定语言环境的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）。
 */
class ReplaceLocalisationWithTranslationFromLocaleIntention : ManipulateLocalisationIntentionBase.WithLocalePopup() {
    override fun getFamilyName() = ChronicleBundle.message("intention.replaceLocalisationWithTranslationFromLocale")

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return super.isAvailable(project, editor, file) && TranslationToolService.getInstance().findTool() != null
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile, context: Context) {
        val (elements, selectedLocale) = context
        withBackgroundProgress(project, ChronicleBundle.message("intention.replaceLocalisationWithTranslationFromLocale.progress.title", selectedLocale.text)) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationManipulationContext.create(it) }.toList() }
            val contextsToHandle = contexts.filter { context -> context.needProcess }
            val errorRef = AtomicReference<Throwable>()

            runCatchingCancelable r@{
                if (contextsToHandle.isEmpty()) return@r
                reportProgressScope(contextsToHandle.size) { reporter ->
                    contextsToHandle.forEachConcurrent f@{ context ->
                        reporter.itemStep(ChronicleBundle.message("manipulation.localisation.search.translate.replace.progress.itemStep", context.key)) {
                            withErrorRef(errorRef) { handleText(context, project, selectedLocale) }.getOrThrow()
                            withErrorRef(errorRef) { replaceText(context, project) }.getOrNull()
                        }
                    }
                }
            }

            createNotification(selectedLocale, errorRef.get())
                .addAction(ParadoxLocalisationManipulationService.createRevertAction(contexts))
                .addAction(ParadoxLocalisationManipulationService.createReapplyAction(contexts))
                .notify(project)
        }
    }

    private suspend fun handleText(context: ParadoxLocalisationManipulationContext, project: Project, selectedLocale: CwtLocaleConfig) {
        ParadoxLocalisationManipulationService.searchTextFromLocale(context, project, selectedLocale)
        val locale = selectLocale(context.element) ?: return
        ParadoxLocalisationManipulationService.handleTextWithTranslation(context, selectedLocale, locale)
    }

    private suspend fun replaceText(context: ParadoxLocalisationManipulationContext, project: Project) {
        val commandName = ChronicleBundle.message("manipulation.localisation.command.translate.replace")
        ParadoxLocalisationManipulationService.replaceText(context, project, commandName)
    }

    private fun createNotification(selectedLocale: CwtLocaleConfig, error: Throwable?): Notification {
        if (error == null) {
            val content = ChronicleBundle.message("intention.replaceLocalisationWithTranslationFromLocale.notification", selectedLocale.text, Messages.success())
            return ChronicleNotificationGroups.manipulation().createNotification(content, NotificationType.INFORMATION)
        }

        thisLogger().warn(error)
        val errorDetails = error.message?.let { ChronicleBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = ChronicleBundle.message("intention.replaceLocalisationWithTranslationFromLocale.notification", selectedLocale.text, Messages.failed()) + errorDetails
        return ChronicleNotificationGroups.manipulation().createNotification(content, NotificationType.WARNING)
    }
}
