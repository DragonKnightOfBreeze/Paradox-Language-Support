package icu.windea.pls.ai.actions.localisation

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.coroutines.forEachConcurrent
import com.intellij.platform.util.progress.reportRawProgress
import icu.windea.pls.PlsBundle
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.model.requests.TranslateLocalisationAiRequest
import icu.windea.pls.ai.model.results.LocalisationAiResult
import icu.windea.pls.ai.util.PlsAiManager
import icu.windea.pls.ai.util.manipulators.ParadoxLocalisationAiManipulator
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.actions.localisation.ManipulateLocalisationActionBase
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationManipulator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class AiReplaceLocalisationWithTranslationAction : ManipulateLocalisationActionBase.WithLocalePopupAndPopup<String>(), DumbAware {
    override fun isAvailable(e: AnActionEvent, project: Project): Boolean {
        return super.isAvailable(e, project) && PlsAiFacade.isAvailable()
    }

    override fun createPopup(e: AnActionEvent, project: Project, callback: (String) -> Unit): JBPopup {
        return ParadoxLocalisationAiManipulator.createPopup(project, callback)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("UnstableApiUsage")
    override suspend fun doHandleAll(e: AnActionEvent, project: Project, context: Context<String>) {
        //进度条 - 显示的进度文本不会变化，按已处理的文件来显示当前进度
        //并发性 - 文件级别

        val (files, selectedLocale, data) = context
        val description = ParadoxLocalisationAiManipulator.getOptimizedDescription(data)
        withBackgroundProgress(project, PlsBundle.message("ai.action.replaceLocalisationWithTranslation.progress.title", selectedLocale.text)) action@{
            val total = files.size
            val allContexts = mutableListOf<ParadoxLocalisationContext>().synced()
            val processedRef = AtomicInteger()
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false

            reportRawProgress { rawReporter ->
                val stepText = PlsBundle.message("manipulation.localisation.translate.replace.progress.filesStep", total)
                rawReporter.text(stepText)

                files.forEachConcurrent { file ->
                    val elements = findElements(e, file)
                    val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) }.toList() }
                    val contextsToHandle = contexts.filter { context -> context.shouldHandle }
                    allContexts.addAll(contextsToHandle)

                    if (contextsToHandle.isNotEmpty()) {
                        val request = TranslateLocalisationAiRequest(project, file, contextsToHandle, selectedLocale, description)
                        val callback: suspend (LocalisationAiResult) -> Unit = {
                            val context = request.localisationContexts[request.index]
                            runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
                        }
                        runCatchingCancelable { handleText(request, callback) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()

                        //不期望的结果，但是不报错（假定这是因为AI仅翻译了部分条目导致的）
                        if (request.index != contextsToHandle.size) withWarnings = true
                    }

                    val processed = processedRef.incrementAndGet()
                    rawReporter.fraction(processed / total.toDouble())
                    rawReporter.text("$stepText ($processed / $total)")
                }
            }

            createNotification(selectedLocale, processedRef.get(), errorRef.get(), withWarnings)
                .addAction(ParadoxLocalisationManipulator.createRevertAction(allContexts))
                .addAction(ParadoxLocalisationManipulator.createReapplyAction(allContexts))
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

    private fun createNotification(selectedLocale: CwtLocaleConfig, processed: Int, error: Throwable?, withWarnings: Boolean): Notification {
        if (error == null) {
            if (!withWarnings) {
                val content = PlsBundle.message("ai.action.replaceLocalisationWithTranslation.notification", selectedLocale.text, Messages.success(processed))
                return PlsCoreManager.createNotification(NotificationType.INFORMATION, content)
            }
            val content = PlsBundle.message("ai.action.replaceLocalisationWithTranslation.notification", selectedLocale.text, Messages.partialSuccess(processed))
            return PlsCoreManager.createNotification(NotificationType.WARNING, content)
        }

        thisLogger().warn(error)
        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("ai.action.replaceLocalisationWithTranslation.notification", selectedLocale.text, Messages.partialSuccess(processed)) + errorDetails
        return PlsCoreManager.createNotification(NotificationType.WARNING, content)
    }
}
