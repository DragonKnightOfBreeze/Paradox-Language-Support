package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import com.intellij.platform.util.progress.reportProgress
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.integrations.translation.PlsTranslationManager
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationManipulator
import icu.windea.pls.lang.withErrorRef
import java.util.concurrent.atomic.AtomicReference

/**
 * 替换为翻译后来自指定语言区域的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）。
 */
class ReplaceLocalisationWithTranslationFromLocaleIntention : ManipulateLocalisationIntentionBase.WithLocalePopup() {
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

            run {
                if(contextsToHandle.isEmpty()) return@run
                val locale = selectLocale(file)
                reportProgress(contextsToHandle.size) { reporter ->
                    contextsToHandle.forEachConcurrent f@{ context ->
                        reporter.itemStep(PlsBundle.message("manipulation.localisation.search.translate.replace.progress.itemStep", context.key)) {
                            withErrorRef(errorRef) { handleText(context, project, selectedLocale, locale) }.getOrThrow()
                            withErrorRef(errorRef) { replaceText(context, project) }.getOrNull()
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

    private suspend fun handleText(context: ParadoxLocalisationContext, project: Project, selectedLocale: CwtLocaleConfig, locale: CwtLocaleConfig?) {
        ParadoxLocalisationManipulator.searchTextFromLocale(context, project, selectedLocale)
        if (locale != null) ParadoxLocalisationManipulator.handleTextWithTranslation(context, locale)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.translate.replace")
        ParadoxLocalisationManipulator.replaceText(context, project, commandName)
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
