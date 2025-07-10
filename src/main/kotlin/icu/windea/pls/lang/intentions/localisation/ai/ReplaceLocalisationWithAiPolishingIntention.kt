package icu.windea.pls.lang.intentions.localisation.ai

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.platform.ide.progress.*
import com.intellij.platform.util.coroutines.*
import com.intellij.platform.util.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.intentions.localisation.*
import icu.windea.pls.lang.util.manipulators.*
import java.util.concurrent.atomic.*

/**
 * （基于AI）替换为翻译后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）。
 */
class ReplaceLocalisationWithAiPolishingIntention : ManipulateLocalisationIntentionBase.WithPopup<String>() {
    override fun getFamilyName() = PlsBundle.message("intention.replaceLocalisationWithAiPolishing")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return super.isAvailable(project, editor, file) && PlsAiManager.isAvailable()
    }

    override fun createPopup(project: Project, editor: Editor?, file: PsiFile?, callback: (String) -> Unit): JBPopup {
        return PlsAiManager.getPolishLocalisationService().createDescriptionPopup(project, callback)
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile?, context: Context<String>) {
        val (elements, data) = context
        withBackgroundProgress(project, PlsBundle.message("intention.replaceLocalisationWithAiPolishing.progress.title")) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) } }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false

            if (contextsToHandle.isNotEmpty()) {
                val total = contextsToHandle.size
                var current = 0
                val chunkSize = PlsAiManager.getSettings().features.batchSizeOfLocalisations
                val contextsChunked = contextsToHandle.chunked(chunkSize)
                reportRawProgress p@{ reporter ->
                    reporter.text(PlsBundle.message("manipulation.localisation.polish.replace.progress.step"))

                    contextsChunked.forEachConcurrent f@{ inputContexts ->
                        val inputText = inputContexts.joinToString("\n") { context -> context.join() }
                        val request = PlsAiPolishLocalisationsRequest(inputContexts, inputText, data, file, project)
                        val callback: suspend (ParadoxLocalisationResult) -> Unit = { data ->
                            val context = request.inputContexts[request.index]
                            runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()

                            current++
                            reporter.text(PlsBundle.message("manipulation.localisation.polish.replace.progress.itemStep", data.key))
                            reporter.fraction(current / total.toDouble())
                        }
                        runCatchingCancelable { handleText(request, callback) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
                        if (request.index != inputContexts.size) { //不期望的结果，但是不报错（假定这是因为AI仅翻译了部分条目导致的）
                            withWarnings = true
                            current += inputContexts.size - request.index
                        }
                    }
                }
            }

            createNotification(contextsToHandle, errorRef.get(), withWarnings)
                .addAction(ParadoxLocalisationManipulator.createRevertAction(contexts))
                .addAction(ParadoxLocalisationManipulator.createReapplyAction(contexts))
                .notify(project)
        }
    }

    private suspend fun handleText(request: PlsAiPolishLocalisationsRequest, callback: suspend (ParadoxLocalisationResult) -> Unit) {
        ParadoxLocalisationManipulator.handleTextWithAiPolishing(request, callback)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.ai.polish.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createNotification(contexts: List<ParadoxLocalisationContext>, error: Throwable?, withWarnings: Boolean): Notification {
        if (error == null) {
            if (!withWarnings) {
                val content = PlsBundle.message("intention.replaceLocalisationWithAiPolishing.notification", Messages.success())
                return createNotification(content, NotificationType.INFORMATION)
            }
            val content = PlsBundle.message("intention.replaceLocalisationWithAiPolishing.notification", Messages.partialSuccess())
            return createNotification(content, NotificationType.WARNING)
        }

        thisLogger().warn(error)
        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("intention.replaceLocalisationWithAiPolishing.notification", Messages.partialSuccess()) + errorDetails
        return createNotification(content, NotificationType.WARNING)
    }
}
