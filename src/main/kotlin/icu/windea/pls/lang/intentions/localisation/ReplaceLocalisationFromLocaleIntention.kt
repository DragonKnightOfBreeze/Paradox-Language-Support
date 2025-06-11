package icu.windea.pls.lang.intentions.localisation

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.command.*
import com.intellij.openapi.diagnostic.thisLogger
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
import java.util.concurrent.atomic.*

/**
 * 替换为来自特定语言区域的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）。
 */
class ReplaceLocalisationFromLocaleIntention : ReplaceLocalisationIntentionBase() {
    override fun getFamilyName() = PlsBundle.message("intention.replaceLocalisationFromLocale")

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig?) {
        if (selectedLocale == null) return
        withBackgroundProgress(project, PlsBundle.message("intention.replaceLocalisationFromLocale.progress.title", selectedLocale)) action@{
            val elementsAndSnippets = elements.map { it to readAction { ParadoxLocalisationSnippets.from(it) } }
            val elementsAndSnippetsToHandle = elementsAndSnippets.filter { (_, snippets) -> snippets.text.isNotBlank() }
            val errorRef = AtomicReference<Throwable>()

            reportSequentialProgress { seqReporter ->
                seqReporter.nextStep(50) {
                    reportProgress(elementsAndSnippetsToHandle.size) { reporter ->
                        elementsAndSnippetsToHandle.forEachConcurrent f@{ (_, snippets) ->
                            reporter.itemStep(PlsBundle.message("intention.localisation.search.progress.step", snippets.key)) {
                                runCatchingCancelable { doHandleText(project, file, snippets, selectedLocale) }.onFailure { errorRef.set(it) }.getOrThrow()
                            }
                        }
                    }
                }

                if (errorRef.get() != null) {
                    return@action createFailedNotification(project, selectedLocale, errorRef.get())
                }

                seqReporter.nextStep(100) {
                    reportProgress(elementsAndSnippetsToHandle.size) { reporter ->
                        elementsAndSnippetsToHandle.forEachConcurrent f@{ (element, snippets) ->
                            reporter.itemStep(PlsBundle.message("intention.localisation.replace.progress.step", snippets.key)) {
                                runCatchingCancelable { doReplaceText(project, file, element, snippets) }.onFailure { errorRef.set(it) }.getOrNull()
                            }
                        }
                    }
                }

                if (errorRef.get() != null) {
                    return@action createFailedNotification(project, selectedLocale, errorRef.get())
                }
            }

            createSuccessNotification(project, selectedLocale)
        }
    }

    private suspend fun doHandleText(project: Project, file: PsiFile?, snippets: ParadoxLocalisationSnippets, selectedLocale: CwtLocaleConfig) {
        val newText = readAction {
            val selector = selector(project, file).localisation().contextSensitive().locale(selectedLocale)
            val e = ParadoxLocalisationSearch.search(snippets.key, selector).find() ?: return@readAction null
            e.value
        }
        if (newText == null) return
        snippets.newText = newText
    }

    @Suppress("UnstableApiUsage")
    private suspend fun doReplaceText(project: Project, file: PsiFile?, element: ParadoxLocalisationProperty, snippets: ParadoxLocalisationSnippets) {
        val newText = snippets.newText
        writeCommandAction(project, PlsBundle.message("intention.localisation.command.replace")) {
            element.setValue(newText)
        }
    }

    private fun createSuccessNotification(project: Project, selectedLocale: CwtLocaleConfig) {
        val content = PlsBundle.message("intention.replaceLocalisationFromLocale.notification.0", selectedLocale)
        createNotification(content, NotificationType.INFORMATION).notify(project)
    }

    private fun createFailedNotification(project: Project, selectedLocale: CwtLocaleConfig, error: Throwable) {
        thisLogger().warn(error)

        val errorDetails = error.message?.let { "<br>$it" }.orEmpty()
        val content = PlsBundle.message("intention.replaceLocalisationFromLocale.notification.1", selectedLocale) + errorDetails
        createNotification(content, NotificationType.WARNING).notify(project)
    }
}
