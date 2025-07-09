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
import icu.windea.pls.localisation.psi.*
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
        return PlsAiManager.getPolishLocalisationService().createDescriptionPopup(project, editor, file, callback)
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, data: String?) {
        withBackgroundProgress(project, PlsBundle.message("intention.replaceLocalisationWithAiPolishing.progress.title")) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) } }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false

            if (contextsToHandle.isNotEmpty()) {
                val total = contextsToHandle.size.toDouble()
                var current = 0
                val chunkSize = PlsAiManager.getSettings().features.batchSizeOfLocalisations
                val contextsChunked = contextsToHandle.chunked(chunkSize)
                reportRawProgress p@{ reporter ->
                    reporter.text(PlsBundle.message("intention.localisation.polish.replace.progress.initStep"))

                    contextsChunked.forEachConcurrent f@{ inputContexts ->
                        val inputText = inputContexts.joinToString("\n") { context -> context.join() }
                        val request = PlsAiPolishLocalisationsRequest(inputContexts, inputText, data, file, project)
                        val callback: suspend (ParadoxLocalisationResult) -> Unit = { data ->
                            val context = request.inputContexts[request.index]
                            runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()

                            current++
                            reporter.text(PlsBundle.message("intention.localisation.polish.progress.step", data.key))
                            reporter.fraction(current / total)
                        }
                        runCatchingCancelable { handleText(request, callback) }.onFailure { errorRef.set(it) }.getOrNull()
                        if (request.index != inputContexts.size) { //不期望的结果，但是不报错（假定这是因为AI仅翻译了部分条目导致的）
                            withWarnings = true
                            current += inputContexts.size - request.index
                        }
                    }
                }
            }

            if (errorRef.get() != null) {
                return@action createFailedNotification(project, errorRef.get())
            }

            if (withWarnings) {
                return@action createSuccessWithWarningsNotification(project)
            }
            createSuccessNotification(project)
        }
    }

    private suspend fun handleText(request: PlsAiPolishLocalisationsRequest, callback: suspend (ParadoxLocalisationResult) -> Unit) {
        ParadoxLocalisationManipulator.handleTextWithAiPolishing(request, callback)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("intention.localisation.command.ai.polish.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createSuccessNotification(project: Project) {
        val content = PlsBundle.message("intention.replaceLocalisationWithAiPolishing.notification.0")
        createNotification(content, NotificationType.INFORMATION).notify(project)
    }

    private fun createSuccessWithWarningsNotification(project: Project) {
        val content = PlsBundle.message("intention.replaceLocalisationWithAiPolishing.notification.2")
        createNotification(content, NotificationType.WARNING).notify(project)
    }

    private fun createFailedNotification(project: Project, error: Throwable) {
        thisLogger().warn(error)

        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("intention.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("intention.replaceLocalisationWithAiPolishing.notification.1") + errorDetails
        createNotification(content, NotificationType.WARNING).notify(project)
    }
}
