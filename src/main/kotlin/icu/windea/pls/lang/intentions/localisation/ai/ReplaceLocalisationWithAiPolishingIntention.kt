package icu.windea.pls.lang.intentions.localisation.ai

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.platform.ide.progress.*
import com.intellij.platform.util.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.model.requests.*
import icu.windea.pls.ai.model.results.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.ai.util.manipulators.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.intentions.localisation.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.manipulators.*
import java.util.concurrent.atomic.*

/**
 * （基于AI）替换为翻译后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）。
 */
class ReplaceLocalisationWithAiPolishingIntention : ManipulateLocalisationIntentionBase.WithPopup<String>(), DumbAware {
    override fun getFamilyName() = PlsBundle.message("intention.replaceLocalisationWithAiPolishing")

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        return super.isAvailable(project, editor, file) && PlsAiFacade.isAvailable()
    }

    override fun createPopup(project: Project, editor: Editor, file: PsiFile, callback: (String) -> Unit): JBPopup {
        return ParadoxLocalisationAiManipulator.createPopup(project, callback)
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile, context: Context<String>) {
        val (elements, data) = context
        val description = ParadoxLocalisationAiManipulator.getOptimizedDescription(data)
        withBackgroundProgress(project, PlsBundle.message("intention.replaceLocalisationWithAiPolishing.progress.title")) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) }.toList() }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false

            if (contextsToHandle.isNotEmpty()) {
                val total = contextsToHandle.size
                var current = 0
                reportRawProgress p@{ reporter ->
                    reporter.text(PlsBundle.message("manipulation.localisation.polish.replace.progress.step"))

                    val request = PolishLocalisationAiRequest(project, file, contextsToHandle, description)
                    val callback: suspend (LocalisationAiResult) -> Unit = { data ->
                        val context = request.localisationContexts[request.index]
                        runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()

                        current++
                        reporter.text(PlsBundle.message("manipulation.localisation.polish.replace.progress.itemStep", data.key))
                        reporter.fraction(current / total.toDouble())
                    }
                    runCatchingCancelable { handleText(request, callback) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()

                    //不期望的结果，但是不报错（假定这是因为AI仅翻译了部分条目导致的）
                    if (request.index != contextsToHandle.size) withWarnings = true
                }
            }

            createNotification(errorRef.get(), withWarnings)
                .addAction(ParadoxLocalisationManipulator.createRevertAction(contextsToHandle))
                .addAction(ParadoxLocalisationManipulator.createReapplyAction(contextsToHandle))
                .notify(project)
        }
    }

    private suspend fun handleText(request: PolishLocalisationAiRequest, callback: suspend (LocalisationAiResult) -> Unit) {
        ParadoxLocalisationAiManipulator.handleTextWithAiPolishing(request, callback)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.ai.polish.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createNotification(error: Throwable?, withWarnings: Boolean): Notification {
        if (error == null) {
            if (!withWarnings) {
                val content = PlsBundle.message("intention.replaceLocalisationWithAiPolishing.notification", Messages.success())
                return PlsCoreManager.createNotification(NotificationType.INFORMATION, content)
            }
            val content = PlsBundle.message("intention.replaceLocalisationWithAiPolishing.notification", Messages.partialSuccess())
            return PlsCoreManager.createNotification(NotificationType.WARNING, content)
        }

        thisLogger().warn(error)
        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("intention.replaceLocalisationWithAiPolishing.notification", Messages.partialSuccess()) + errorDetails
        return PlsCoreManager.createNotification(NotificationType.WARNING, content)
    }
}
