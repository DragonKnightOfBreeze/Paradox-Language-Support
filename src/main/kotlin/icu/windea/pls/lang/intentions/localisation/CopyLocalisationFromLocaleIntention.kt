package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.platform.ide.progress.*
import com.intellij.platform.util.coroutines.*
import com.intellij.platform.util.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.manipulators.*
import java.awt.datatransfer.*
import java.util.concurrent.atomic.*

/**
 * 复制来自特定语言区域的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationFromLocaleIntention : ManipulateLocalisationIntentionBase.WithLocalePopup() {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationFromLocale")

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile, context: Context) {
        val (elements, selectedLocale) = context
        withBackgroundProgress(project, PlsBundle.message("intention.copyLocalisationFromLocale.progress.title", selectedLocale.text)) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) }.toList() }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()

            if (contextsToHandle.isNotEmpty()) {
                reportProgress(contextsToHandle.size) { reporter ->
                    contextsToHandle.forEachConcurrent f@{ context ->
                        reporter.itemStep(PlsBundle.message("manipulation.localisation.search.progress.itemStep", context.key)) {
                            runCatchingCancelable { handleText(context, project, selectedLocale) }.onFailure { errorRef.compareAndSet(null, it) }.getOrThrow()
                        }
                    }
                }
            }

            if (errorRef.get() == null) {
                val textToCopy = ParadoxLocalisationManipulator.joinText(contexts)
                CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
            }
            createNotification(selectedLocale, errorRef.get()).notify(project)
        }
    }

    private suspend fun handleText(context: ParadoxLocalisationContext, project: Project, selectedLocale: CwtLocaleConfig) {
        return ParadoxLocalisationManipulator.handleTextFromLocale(context, project, selectedLocale)
    }

    private fun createNotification(selectedLocale: CwtLocaleConfig, error: Throwable?): Notification {
        if (error == null) {
            val content = PlsBundle.message("intention.copyLocalisationFromLocale.notification", selectedLocale.text, Messages.success())
            return PlsCoreManager.createNotification(NotificationType.INFORMATION, content)
        }

        thisLogger().warn(error)
        val errorDetails = error.message?.let { PlsBundle.message("manipulation.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("intention.copyLocalisationFromLocale.notification", selectedLocale.text, Messages.failed()) + errorDetails
        return PlsCoreManager.createNotification(NotificationType.WARNING, content)
    }
}
