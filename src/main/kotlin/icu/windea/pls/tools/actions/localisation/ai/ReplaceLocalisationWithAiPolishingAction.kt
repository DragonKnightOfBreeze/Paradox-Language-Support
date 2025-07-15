package icu.windea.pls.tools.actions.localisation.ai

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
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.coroutines.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.tools.actions.localisation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.atomic.*

class ReplaceLocalisationWithAiPolishingAction : ManipulateLocalisationActionBase.WithPopup<String>() {
    override fun isAvailable(e: AnActionEvent, project: Project): Boolean {
        return super.isAvailable(e, project) && PlsAiManager.isAvailable()
    }

    override fun createPopup(e: AnActionEvent, project: Project, callback: (String) -> Unit): JBPopup? {
        return PlsAiManager.getPolishLocalisationService().createDescriptionPopup(project, callback)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Suppress("UnstableApiUsage")
    override suspend fun doHandleAll(e: AnActionEvent, project: Project, context: Context<String>) {
        //进度条 - 显示的进度文本不会变化，按已处理的文件来显示当前进度
        //并发性 - 文件级别+本地化组级别（需要处理的本地化每次达到chunkSize）

        val (files) = context
        withBackgroundProgress(project, PlsBundle.message("action.replaceLocalisationWithAiPolishing.progress.title")) action@{
            val total = files.size
            val contexts = mutableListOf<ParadoxLocalisationContext>().synced()
            val processedRef = AtomicInteger()
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false

            reportRawProgress { rawReporter ->
                val stepText = PlsBundle.message("manipulation.localisation.polish.replace.progress.filesStep", total)
                rawReporter.text(stepText)

                files.forEachConcurrent { file ->
                    val chunkSize = PlsAiManager.getSettings().features.batchSizeOfLocalisations
                    val elements = ParadoxLocalisationManipulator.buildFlow(file)
                    elements.transform { element ->
                        val context = readAction { ParadoxLocalisationContext.from(element) }
                        if (!context.shouldHandle) return@transform
                        emit(context)
                    }.toChunkedFlow(chunkSize).flatMapMerge { inputContexts ->
                        flow {
                            val inputText = inputContexts.joinToString("\n") { context -> context.join() }
                            val request = PlsAiPolishLocalisationRequest(project, file, inputContexts, inputText, null)
                            val callback: suspend (ParadoxLocalisationResult) -> Unit = { data ->
                                val context = request.inputContexts[request.index]
                                runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
                            }
                            runCatchingCancelable { handleText(request, callback) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
                            if (request.index != inputContexts.size) {
                                withWarnings = true
                            }
                            inputContexts.forEach { emit(it) }
                        }
                    }.collect { context -> contexts += context }

                    val processed = processedRef.incrementAndGet()
                    rawReporter.fraction(processed / total.toDouble())
                    rawReporter.text("$stepText ($processed / $total)")
                }
            }

            createNotification(contexts, processedRef.get(), errorRef.get(), withWarnings)
                .addAction(ParadoxLocalisationManipulator.createRevertAction(contexts))
                .addAction(ParadoxLocalisationManipulator.createReapplyAction(contexts))
                .notify(project)
        }
    }

    private suspend fun handleText(request: PlsAiPolishLocalisationRequest, callback: suspend (ParadoxLocalisationResult) -> Unit) {
        ParadoxLocalisationManipulator.handleTextWithAiPolishing(request, callback)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.ai.polish.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createNotification(contexts: List<ParadoxLocalisationContext>, processed: Int, error: Throwable?, withWarnings: Boolean): Notification {
        if (error == null) {
            if (!withWarnings) {
                val content = PlsBundle.message("action.replaceLocalisationWithAiPolishing.notification", Messages.success(processed))
                return createNotification(content, NotificationType.INFORMATION)
            }
            val content = PlsBundle.message("action.replaceLocalisationWithAiPolishing.notification", Messages.partialSuccess(processed))
            return createNotification(content, NotificationType.WARNING)
        }

        thisLogger().warn(error)
        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("action.replaceLocalisationWithAiPolishing.notification", Messages.partialSuccess(processed)) + errorDetails
        return createNotification(content, NotificationType.WARNING)
    }
}
