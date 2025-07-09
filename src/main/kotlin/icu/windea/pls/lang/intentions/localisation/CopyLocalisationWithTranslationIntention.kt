package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.platform.ide.progress.*
import com.intellij.platform.util.coroutines.*
import com.intellij.platform.util.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.integrations.translation.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.localisation.psi.*
import java.awt.datatransfer.*
import java.util.concurrent.atomic.*

/**
 * 复制翻译后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationWithTranslationIntention : ManipulateLocalisationIntentionBase.WithLocalePopup() {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationWithTranslation")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return super.isAvailable(project, editor, file) && PlsTranslationManager.findTool() != null
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig) {
        withBackgroundProgress(project, PlsBundle.message("intention.copyLocalisationWithTranslation.progress.title", selectedLocale)) action@{
            val contexts = readAction { elements.map { ParadoxLocalisationContext.from(it) } }
            val contextsToHandle = contexts.filter { context -> context.shouldHandle }
            val errorRef = AtomicReference<Throwable>()

            if (contextsToHandle.isNotEmpty()) {
                reportProgress(contextsToHandle.size) { reporter ->
                    contextsToHandle.forEachConcurrent f@{ context ->
                        reporter.itemStep(PlsBundle.message("intention.localisation.translate.progress.step", context.key)) {
                            runCatchingCancelable { handleText(context, selectedLocale) }.onFailure { errorRef.set(it) }.getOrThrow()
                        }
                    }
                }
            }

            if (errorRef.get() != null) {
                return@action createFailedNotification(project, selectedLocale, errorRef.get())
            }

            val textToCopy = ParadoxLocalisationManipulator.joinText(contexts)
            CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
            createSuccessNotification(project, selectedLocale)
        }
    }

    private suspend fun handleText(context: ParadoxLocalisationContext, selectedLocale: CwtLocaleConfig) {
        return ParadoxLocalisationManipulator.handleTextWithTranslation(context, selectedLocale)
    }

    private fun createSuccessNotification(project: Project, selectedLocale: CwtLocaleConfig) {
        val content = PlsBundle.message("intention.copyLocalisationWithTranslation.notification.0", selectedLocale)
        createNotification(content, NotificationType.INFORMATION).notify(project)
    }

    private fun createFailedNotification(project: Project, selectedLocale: CwtLocaleConfig, error: Throwable) {
        thisLogger().warn(error)

        val errorDetails = error.message?.let { PlsBundle.message("intention.localisation.error", it) }.orEmpty()
        val content = PlsBundle.message("intention.copyLocalisationWithTranslation.notification.1", selectedLocale) + errorDetails
        createNotification(content, NotificationType.WARNING).notify(project)
    }
}
