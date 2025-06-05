package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.platform.ide.progress.*
import com.intellij.platform.util.coroutines.*
import com.intellij.platform.util.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import java.awt.datatransfer.*
import java.util.concurrent.atomic.*

/**
 * 复制来自特定语言区域的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationFromLocaleIntention : CopyLocalisationIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.copyLocalisationFromLocale")

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig?) {
        if (selectedLocale == null) return
        withBackgroundProgress(project, PlsBundle.message("intention.copyLocalisationFromLocale.progress.title", selectedLocale)) {
            val elementsAndSnippets = elements.map { it to ParadoxLocalisationSnippets.from(it) }
            val elementsAndSnippetsToHandle = elementsAndSnippets.filter { (_, snippets) -> snippets.text.isNotBlank() }
            val errorRef = AtomicReference<Throwable>()
            if (elementsAndSnippetsToHandle.isNotEmpty()) {
                reportProgress(elementsAndSnippetsToHandle.size) { reporter ->
                    elementsAndSnippetsToHandle.forEachConcurrent f@{ (_, snippets) ->
                        reporter.itemStep(PlsBundle.message("intention.copyLocalisationFromLocale.progress.step")) {
                            runCatchingCancelable { doHandleText(project, file, snippets, selectedLocale) }.onFailure { errorRef.set(it) }.getOrThrow()
                        }
                    }
                }
            }

            if (errorRef.get() == null) {
                val textToCopy = elementsAndSnippets.joinToString("\n") { (_, snippets) -> snippets.joinWithNewText() }
                CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
                val content = PlsBundle.message("intention.copyLocalisationFromLocale.notification.0", selectedLocale)
                createNotification(content, NotificationType.INFORMATION).notify(project)
            } else {
                val errorDetails = errorRef.get().message?.let { "<br>$it" }.orEmpty()
                val content = PlsBundle.message("intention.copyLocalisationFromLocale.notification.1", selectedLocale) + errorDetails
                createNotification(content, NotificationType.WARNING).notify(project)
            }
        }
    }

    private fun doHandleText(project: Project, file: PsiFile?, snippets: ParadoxLocalisationSnippets, selectedLocale: CwtLocaleConfig) {
        val selector = selector(project, file).localisation().contextSensitive().locale(selectedLocale)
        val e = ParadoxLocalisationSearch.search(snippets.key, selector).find() ?: return
        val newText = e.value ?: return
        snippets.newText = newText
    }
}
