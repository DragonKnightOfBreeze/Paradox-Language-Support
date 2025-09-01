package icu.windea.pls.ai.intentions.localisation

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.platform.util.progress.reportRawProgress
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.ai.PlsAiFacade
import icu.windea.pls.ai.model.requests.PolishLocalisationAiRequest
import icu.windea.pls.ai.model.results.LocalisationAiResult
import icu.windea.pls.ai.util.PlsAiManager
import icu.windea.pls.ai.util.manipulators.ParadoxLocalisationAiManipulator
import icu.windea.pls.lang.intentions.localisation.ManipulateLocalisationIntentionBase
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationContext
import icu.windea.pls.lang.util.manipulators.ParadoxLocalisationManipulator
import icu.windea.pls.lang.withErrorRef
import java.awt.datatransfer.StringSelection
import java.util.concurrent.atomic.AtomicReference

/**
 * 【AI】复制润色后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class AiCopyLocalisationWithPolishingIntention : ManipulateLocalisationIntentionBase.WithPopup<String>(), DumbAware {
    override fun getFamilyName() = PlsBundle.message("ai.intention.copyLocalisationWithPolishing")

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return super.isAvailable(project, editor, file) && PlsAiFacade.isAvailable()
    }

    override fun createPopup(project: Project, editor: Editor, file: PsiFile, callback: (String) -> Unit): JBPopup {
        return ParadoxLocalisationAiManipulator.createPopup(project, callback)
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile, context: Context<String>) {
        val (elements, data) = context
        val description = PlsAiManager.getOptimizedDescription(data)
        withBackgroundProgress(project, PlsBundle.message("ai.intention.copyLocalisationWithPolishing.progress.title")) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) }.toList() }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false
            var current = 0

            run {
                if(contextsToHandle.isEmpty()) return@run
                val total = contextsToHandle.size
                reportRawProgress { reporter ->
                    reporter.text(PlsBundle.message("manipulation.localisation.polish.progress.step"))
                    reporter.fraction(0.0)

                    val request = PolishLocalisationAiRequest(project, file, contextsToHandle, description)
                    val callback: suspend (LocalisationAiResult) -> Unit = { data ->
                        current++
                        reporter.text(PlsBundle.message("manipulation.localisation.polish.progress.itemStep", data.key))
                        reporter.fraction(current / total.toDouble())
                    }
                    withErrorRef(errorRef) { handleText(request, callback) }.getOrNull()

                    //不期望的结果，但是不报错（假定这是因为AI仅翻译了部分条目导致的）
                    if (request.index != contextsToHandle.size) withWarnings = true
                }
            }

            if (errorRef.get() == null) {
                val textToCopy = ParadoxLocalisationManipulator.joinText(contexts)
                CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
            }
            createNotification(errorRef.get(), withWarnings).notify(project)
        }
    }

    private suspend fun handleText(request: PolishLocalisationAiRequest, callback: suspend (LocalisationAiResult) -> Unit) {
        ParadoxLocalisationAiManipulator.handleTextWithAiPolishing(request, callback)
    }

    private fun createNotification(error: Throwable?, withWarnings: Boolean): Notification {
        if (error == null) {
            if (!withWarnings) {
                val content = PlsBundle.message("ai.intention.copyLocalisationWithPolishing.notification", Messages.success())
                return PlsCoreManager.createNotification(NotificationType.INFORMATION, content)
            }
            val content = PlsBundle.message("ai.intention.copyLocalisationWithPolishing.notification", Messages.partialSuccess())
            return PlsCoreManager.createNotification(NotificationType.WARNING, content)
        }

        thisLogger().warn(error)
        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("ai.intention.copyLocalisationWithPolishing.notification", Messages.partialSuccess()) + errorDetails
        return PlsCoreManager.createNotification(NotificationType.WARNING, content)
    }
}
