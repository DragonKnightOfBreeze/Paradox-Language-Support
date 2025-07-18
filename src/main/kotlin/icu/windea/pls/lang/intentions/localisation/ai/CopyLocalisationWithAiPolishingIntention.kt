package icu.windea.pls.lang.intentions.localisation.ai

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
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
import java.awt.datatransfer.*
import java.util.concurrent.atomic.*

/**
 * （基于AI）复制润色后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationWithAiPolishingIntention : ManipulateLocalisationIntentionBase.WithPopup<String>(), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationWithAiPolishing")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return super.isAvailable(project, editor, file) && PlsAiManager.isAvailable()
    }

    override fun createPopup(project: Project, editor: Editor?, file: PsiFile?, callback: (String) -> Unit): JBPopup {
        return PlsAiManager.getPolishLocalisationService().createDescriptionPopup(project, callback)
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile?, context: Context<String>) {
        val (elements, data) = context
        withBackgroundProgress(project, PlsBundle.message("intention.copyLocalisationWithAiPolishing.progress.title")) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) } }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false
            var current = 0

            if (contextsToHandle.isNotEmpty()) {
                val total = contextsToHandle.size
                val chunkSize = PlsAiManager.getSettings().features.batchSizeOfLocalisations
                val contextsChunked = contextsToHandle.chunked(chunkSize)
                reportRawProgress p@{ reporter ->
                    reporter.text(PlsBundle.message("manipulation.localisation.polish.progress.step"))

                    contextsChunked.forEachConcurrent f@{ inputContexts ->
                        val inputText = inputContexts.joinToString("\n") { context -> context.join() }
                        val request = PlsAiPolishLocalisationRequest(project, file, inputContexts, inputText, data)
                        val callback: suspend (ParadoxLocalisationResult) -> Unit = { data ->
                            current++
                            reporter.text(PlsBundle.message("manipulation.localisation.polish.progress.itemStep", data.key))
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

            if (errorRef.get() == null) {
                val textToCopy = ParadoxLocalisationManipulator.joinText(contexts)
                CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
            }
            createNotification(errorRef.get(), withWarnings).notify(project)
        }
    }

    private suspend fun handleText(request: PlsAiPolishLocalisationRequest, callback: suspend (ParadoxLocalisationResult) -> Unit) {
        ParadoxLocalisationManipulator.handleTextWithAiPolishing(request, callback)
    }

    private fun createNotification(error: Throwable?, withWarnings: Boolean): Notification {
        if (error == null) {
            if (!withWarnings) {
                val content = PlsBundle.message("intention.copyLocalisationWithAiPolishing.notification", Messages.success())
                return createNotification(content, NotificationType.INFORMATION)
            }
            val content = PlsBundle.message("intention.copyLocalisationWithAiPolishing.notification", Messages.partialSuccess())
            return createNotification(content, NotificationType.WARNING)
        }

        thisLogger().warn(error)
        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("intention.copyLocalisationWithAiPolishing.notification", Messages.partialSuccess()) + errorDetails
        return createNotification(content, NotificationType.WARNING)
    }
}
