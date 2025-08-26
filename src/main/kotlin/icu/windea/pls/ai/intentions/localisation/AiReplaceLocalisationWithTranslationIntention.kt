package icu.windea.pls.ai.intentions.localisation

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportRawProgress
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.model.requests.TranslateLocalisationAiRequest
import icu.windea.pls.ai.model.results.LocalisationAiResult
import icu.windea.pls.ai.util.PlsAiManager
import icu.windea.pls.ai.util.manipulators.ParadoxLocalisationAiManipulator
import icu.windea.pls.config.config.CwtLocaleConfig
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.intentions.localisation.ManipulateLocalisationIntentionBase
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationManipulator
import java.util.concurrent.atomic.AtomicReference

/**
 * 【AI】替换为翻译后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）。
 */
class AiReplaceLocalisationWithTranslationIntention : ManipulateLocalisationIntentionBase.WithLocalePopupAndPopup<String>(), DumbAware {
    override fun getFamilyName() = PlsBundle.message("ai.intention.replaceLocalisationWithTranslation")

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return super.isAvailable(project, editor, file) && PlsAiFacade.isAvailable()
    }

    override fun createPopup(project: Project, editor: Editor, file: PsiFile, callback: (String) -> Unit): JBPopup {
        return ParadoxLocalisationAiManipulator.createPopup(project, callback)
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile, context: Context<String>) {
        val (elements, selectedLocale, data) = context
        val description = ParadoxLocalisationAiManipulator.getOptimizedDescription(data)
        withBackgroundProgress(project, PlsBundle.message("ai.intention.replaceLocalisationWithTranslation.progress.title", selectedLocale.text)) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) }.toList() }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false

            if (contextsToHandle.isNotEmpty()) {
                val total = contextsToHandle.size
                var current = 0
                reportRawProgress p@{ reporter ->
                    reporter.text(PlsBundle.message("manipulation.localisation.translate.replace.progress.step"))

                    val request = TranslateLocalisationAiRequest(project, file, contextsToHandle, selectedLocale, description)
                    val callback: suspend (LocalisationAiResult) -> Unit = { data ->
                        val context = request.localisationContexts[request.index]
                        runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()

                        current++
                        reporter.text(PlsBundle.message("manipulation.localisation.translate.replace.progress.itemStep", data.key))
                        reporter.fraction(current / total.toDouble())
                    }
                    runCatchingCancelable { handleText(request, callback) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()

                    //不期望的结果，但是不报错（假定这是因为AI仅翻译了部分条目导致的）
                    if (request.index != contextsToHandle.size) withWarnings = true
                }
            }

            createNotification(selectedLocale, errorRef.get(), withWarnings)
                .addAction(ParadoxLocalisationManipulator.createRevertAction(contextsToHandle))
                .addAction(ParadoxLocalisationManipulator.createReapplyAction(contextsToHandle))
                .notify(project)
        }
    }

    private suspend fun handleText(request: TranslateLocalisationAiRequest, callback: suspend (LocalisationAiResult) -> Unit) {
        ParadoxLocalisationAiManipulator.handleTextWithAiTranslation(request, callback)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.ai.translate.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createNotification(selectedLocale: CwtLocaleConfig, error: Throwable?, withWarnings: Boolean): Notification {
        if (error == null) {
            if (!withWarnings) {
                val content = PlsBundle.message("ai.intention.replaceLocalisationWithTranslation.notification", selectedLocale.text, Messages.success())
                return PlsCoreManager.createNotification(NotificationType.INFORMATION, content)
            }
            val content = PlsBundle.message("ai.intention.replaceLocalisationWithTranslation.notification", selectedLocale.text, Messages.partialSuccess())
            return PlsCoreManager.createNotification(NotificationType.WARNING, content)
        }

        thisLogger().warn(error)
        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("ai.intention.replaceLocalisationWithTranslation.notification", selectedLocale.text, Messages.partialSuccess()) + errorDetails
        return PlsCoreManager.createNotification(NotificationType.WARNING, content)
    }
}
