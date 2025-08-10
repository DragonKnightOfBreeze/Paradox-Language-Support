package icu.windea.pls.lang.actions.localisation.ai

import com.intellij.notification.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.platform.ide.progress.*
import com.intellij.platform.util.coroutines.*
import com.intellij.platform.util.progress.*
import icu.windea.pls.*
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.ai.util.manipulators.ParadoxLocalisationAiManipulator
import icu.windea.pls.ai.util.manipulators.ParadoxLocalisationAiResult
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.localisation.*
import icu.windea.pls.lang.util.manipulators.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.*

class ReplaceLocalisationWithAiTranslationAction : ManipulateLocalisationActionBase.WithLocalePopupAndPopup<String>(), DumbAware {
    override fun isAvailable(e: AnActionEvent, project: Project): Boolean {
        return super.isAvailable(e, project) && PlsAiFacade.isAvailable()
    }

    override fun createPopup(e: AnActionEvent, project: Project, callback: (String) -> Unit): JBPopup {
        return ParadoxLocalisationAiManipulator.createDescriptionPopup(project, "TRANSLATE", callback)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("UnstableApiUsage")
    override suspend fun doHandleAll(e: AnActionEvent, project: Project, context: Context<String>) {
        //进度条 - 显示的进度文本不会变化，按已处理的文件来显示当前进度
        //并发性 - 文件级别

        val (files, selectedLocale, data) = context
        val description = ParadoxLocalisationAiManipulator.getOptimizedDescription(data)
        withBackgroundProgress(project, PlsBundle.message("action.replaceLocalisationWithAiTranslation.progress.title", selectedLocale.text)) action@{
            val total = files.size
            val allContexts = mutableListOf<ParadoxLocalisationContext>().synced()
            val processedRef = AtomicInteger()
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false

            reportRawProgress { rawReporter ->
                val stepText = PlsBundle.message("manipulation.localisation.translate.replace.progress.filesStep", total)
                rawReporter.text(stepText)

                files.forEachConcurrent { file ->
                    val elements = ParadoxLocalisationManipulator.buildSequence(file)
                    val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) }.toList() }
                    val contextsToHandle = contexts.filter { context -> context.shouldHandle }
                    allContexts.addAll(contextsToHandle)

                    val request = TranslateLocalisationAiRequest(project, file, contextsToHandle, selectedLocale, description)
                    val callback: suspend (ParadoxLocalisationAiResult) -> Unit = {
                        val context = request.localisationContexts[request.index]
                        runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
                    }
                    runCatchingCancelable { handleText(request, callback) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()

                    //不期望的结果，但是不报错（假定这是因为AI仅翻译了部分条目导致的）
                    if (request.index != contextsToHandle.size) withWarnings = true

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

    private suspend fun handleText(request: TranslateLocalisationAiRequest, callback: suspend (ParadoxLocalisationAiResult) -> Unit) {
        ParadoxLocalisationAiManipulator.handleTextWithAiTranslation(request, callback)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.ai.translate.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createNotification(selectedLocale: CwtLocaleConfig, processed: Int, error: Throwable?, withWarnings: Boolean): Notification {
        if (error == null) {
            if (!withWarnings) {
                val content = PlsBundle.message("action.replaceLocalisationWithAiTranslation.notification", selectedLocale.text, Messages.success(processed))
                return createNotification(content, NotificationType.INFORMATION)
            }
            val content = PlsBundle.message("action.replaceLocalisationWithAiTranslation.notification", selectedLocale.text, Messages.partialSuccess(processed))
            return createNotification(content, NotificationType.WARNING)
        }

        thisLogger().warn(error)
        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("action.replaceLocalisationWithAiTranslation.notification", selectedLocale.text, Messages.partialSuccess(processed)) + errorDetails
        return createNotification(content, NotificationType.WARNING)
    }
}
