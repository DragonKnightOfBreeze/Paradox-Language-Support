package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import com.intellij.platform.util.progress.reportProgress
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.integrations.translation.PlsTranslationManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationManipulator
import java.util.concurrent.atomic.AtomicReference

/**
 * 替换为翻译后来自指定语言区域的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）。
 */
class ReplaceLocalisationWithTranslationFromLocaleIntention : ManipulateLocalisationIntentionBase.WithLocalePopup(), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.replaceLocalisationWithTranslationFromLocale")

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return super.isAvailable(project, editor, file) && PlsTranslationManager.findTool() != null
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile, context: Context) {
        val (elements, selectedLocale) = context
        withBackgroundProgress(project, PlsBundle.message("intention.replaceLocalisationWithTranslationFromLocale.progress.title", selectedLocale.text)) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) }.toList() }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()

            if (contextsToHandle.isNotEmpty()) {
                reportProgress(contextsToHandle.size) { reporter ->
                    contextsToHandle.forEachConcurrent f@{ context ->
                        reporter.itemStep(PlsBundle.message("manipulation.localisation.translate.replace.progress.itemStep", context.key)) {
                            runCatchingCancelable { handleText(context, selectedLocale) }.onFailure { errorRef.compareAndSet(null, it) }.getOrThrow()
                            runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
                        }
                    }
                }
            }

            createNotification(selectedLocale, errorRef.get())
                .addAction(ParadoxLocalisationManipulator.createRevertAction(contexts))
                .addAction(ParadoxLocalisationManipulator.createReapplyAction(contexts))
                .notify(project)
        }
    }

    private suspend fun handleText(context: ParadoxLocalisationContext, selectedLocale: CwtLocaleConfig) {
        TODO("TL")
        // return ParadoxLocalisationManipulator.handleTextWithTranslation(context, selectedLocale)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.translate.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createNotification(selectedLocale: CwtLocaleConfig, error: Throwable?): Notification {
        if (error == null) {
            val content = PlsBundle.message("intention.replaceLocalisationWithTranslationFromLocale.notification", selectedLocale.text, Messages.success())
            return PlsCoreManager.createNotification(NotificationType.INFORMATION, content)
        }

        thisLogger().warn(error)
        val errorDetails = error.message?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("intention.replaceLocalisationWithTranslationFromLocale.notification", selectedLocale.text, Messages.failed()) + errorDetails
        return PlsCoreManager.createNotification(NotificationType.WARNING, content)
    }
}
