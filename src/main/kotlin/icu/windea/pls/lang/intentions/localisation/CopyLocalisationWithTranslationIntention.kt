package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.*
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
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import java.awt.datatransfer.*
import java.util.concurrent.atomic.*

/**
 * 复制翻译后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationWithTranslationIntention : CopyLocalisationIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationWithTranslation")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (PlsTranslationManager.findTool() == null) return false
        return super.isAvailable(project, editor, file)
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig?) {
        if (selectedLocale == null) return
        withBackgroundProgress(project, PlsBundle.message("intention.copyLocalisationWithTranslation.progress.title", selectedLocale)) {
            val elementsAndSnippets = elements.map { it to ParadoxLocalisationSnippets.from(it) }
            val elementsAndSnippetsToHandle = elementsAndSnippets.filter { (_, snippets) -> snippets.text.isNotBlank() }
            val errorRef = AtomicReference<Throwable>()
            if (elementsAndSnippetsToHandle.isNotEmpty()) {
                reportProgress(elementsAndSnippetsToHandle.size) { reporter ->
                    elementsAndSnippetsToHandle.forEachConcurrent f@{ (element, snippets) ->
                        reporter.itemStep(PlsBundle.message("intention.copyLocalisationWithTranslation.progress.step")) {
                            runCatchingCancelable { doHandleText(element, snippets, selectedLocale) }.onFailure { errorRef.set(it) }.getOrThrow()
                        }
                    }
                }
            }

            if (errorRef.get() == null) {
                val textToCopy = elementsAndSnippets.joinToString("\n") { (_, snippets) -> snippets.joinWithNewText() }
                CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
                val content = PlsBundle.message("intention.copyLocalisationWithTranslation.notification.0", selectedLocale)
                createNotification(content, NotificationType.INFORMATION).notify(project)
            } else {
                val errorDetails = errorRef.get().message?.let { "<br>$it" }.orEmpty()
                val content = PlsBundle.message("intention.copyLocalisationWithTranslation.notification.1", selectedLocale) + errorDetails
                createNotification(content, NotificationType.WARNING).notify(project)
            }
        }
    }

    private fun doHandleText(element: ParadoxLocalisationProperty, snippets: ParadoxLocalisationSnippets, selectedLocale: CwtLocaleConfig) {
        val sourceLocale = selectLocale(element)
        val newText = PlsTranslationManager.translate(snippets.text, sourceLocale, selectedLocale) ?: return
        snippets.newText = newText
    }
}
