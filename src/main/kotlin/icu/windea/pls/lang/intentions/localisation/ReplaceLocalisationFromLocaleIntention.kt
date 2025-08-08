package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.project.*
import com.intellij.platform.ide.progress.*
import com.intellij.platform.util.coroutines.*
import com.intellij.platform.util.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import java.util.concurrent.atomic.*

/**
 * 替换为来自特定语言区域的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）。
 */
class ReplaceLocalisationFromLocaleIntention : ManipulateLocalisationIntentionBase.WithLocalePopup() {
    override fun getFamilyName() = PlsBundle.message("intention.replaceLocalisationFromLocale")

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile, context: Context) {
        val (elements, selectedLocale) = context
        withBackgroundProgress(project, PlsBundle.message("intention.replaceLocalisationFromLocale.progress.title", selectedLocale)) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) }.toList() }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()

            if (contextsToHandle.isNotEmpty()) {
                reportProgress(contextsToHandle.size) { reporter ->
                    contextsToHandle.forEachConcurrent f@{ context ->
                        reporter.itemStep(PlsBundle.message("manipulation.localisation.search.replace.progress.itemStep", context.key)) {
                            runCatchingCancelable { handleText(context, project, selectedLocale) }.onFailure { errorRef.compareAndSet(null, it) }.getOrThrow()
                            runCatchingCancelable { replaceText(context, project) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
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

    private suspend fun handleText(context: ParadoxLocalisationContext, project: Project, selectedLocale: CwtLocaleConfig) {
        return ParadoxLocalisationManipulator.handleTextFromLocale(context, project, selectedLocale)
    }

    private suspend fun replaceText(context: ParadoxLocalisationContext, project: Project) {
        val commandName = PlsBundle.message("manipulation.localisation.command.replace")
        return ParadoxLocalisationManipulator.replaceText(context, project, commandName)
    }

    private fun createNotification(selectedLocale: CwtLocaleConfig, error: Throwable?): Notification {
        if (error == null) {
            val content = PlsBundle.message("intention.replaceLocalisationFromLocale.notification", selectedLocale, Messages.success())
            return createNotification(content, NotificationType.INFORMATION)
        }

        thisLogger().warn(error)
        val errorDetails = error.message?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("intention.replaceLocalisationFromLocale.notification", selectedLocale, Messages.failed()) + errorDetails
        return createNotification(content, NotificationType.WARNING)
    }
}
