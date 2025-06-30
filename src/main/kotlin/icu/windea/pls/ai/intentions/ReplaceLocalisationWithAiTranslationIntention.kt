package icu.windea.pls.ai.intentions

import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.command.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.platform.ide.progress.*
import com.intellij.platform.util.coroutines.*
import com.intellij.platform.util.progress.*
import com.intellij.psi.*
import icu.windea.pls.ai.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.intentions.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import java.util.concurrent.atomic.*

/**
 * （基于AI）替换为翻译后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）。
 */
class ReplaceLocalisationWithAiTranslationIntention : ManipulateLocalisationIntentionBase.WithLocalePopupAndPopup<String>() {
    override fun getFamilyName() = PlsAiBundle.message("intention.replaceLocalisationWithAiTranslation")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return super.isAvailable(project, editor, file) && PlsAiManager.isAvailable()
    }

    override fun createPopup(project: Project, editor: Editor?, file: PsiFile?, callback: (String) -> Unit): JBPopup {
        return PlsAiManager.getTranslateLocalisationService().createDescriptionPopup(project, editor, file, callback)
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig, data: String?) {
        withBackgroundProgress(project, PlsAiBundle.message("intention.replaceLocalisationWithAiTranslation.progress.title", selectedLocale)) action@{
            val elementsAndSnippets = elements.map { it to readAction { ParadoxLocalisationSnippets.from(it) } }
            val elementsAndSnippetsToHandle = elementsAndSnippets.filter { (_, snippets) -> snippets.text.isNotBlank() }
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false

            if (elementsAndSnippetsToHandle.isNotEmpty()) {
                val total = elementsAndSnippetsToHandle.size.toDouble()
                var current = 0
                val chunkSize = PlsAiManager.getSettings().features.batchSizeOfLocalisations
                val elementsAndSnippetsChunked = elementsAndSnippetsToHandle.chunked(chunkSize)
                val aiService = PlsAiManager.getTranslateLocalisationService()
                reportRawProgress p@{ reporter ->
                    reporter.text(PlsAiBundle.message("intention.localisation.translate.replace.progress.initStep"))

                    elementsAndSnippetsChunked.forEachConcurrent f@{ list ->
                        val inputElements = list.map { (element) -> element }
                        val inputText = list.joinToString("\n") { (_, snippets) -> snippets.join() }
                        var i = 0
                        runCatchingCancelable {
                            val request = PlsAiTranslateLocalisationsRequest(inputElements, inputText, data, selectedLocale, file, project)
                            val resultFlow = aiService.translate(request)
                            aiService.checkResultFlow(resultFlow)
                            resultFlow.collect { data ->
                                val (element, snippets) = list[i]
                                aiService.checkOutputData(snippets, data)
                                i++
                                current++
                                reporter.text(PlsAiBundle.message("intention.localisation.translate.replace.progress.step", data.key))
                                reporter.fraction(current / total)

                                snippets.newText = data.text
                                runCatchingCancelable { doReplaceText(project, file, element, snippets) }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
                            }
                        }.onFailure { errorRef.compareAndSet(null, it) }.getOrNull()
                        if (i != list.size) { //不期望的结果，但是不报错（假定这是因为AI仅翻译了部分条目导致的）
                            withWarnings = true
                            current += list.size - i
                        }
                    }
                }
            }

            if (errorRef.get() != null) {
                return@action createFailedNotification(project, selectedLocale, errorRef.get())
            }

            if (withWarnings) {
                return@action createSuccessWithWarningsNotification(project, selectedLocale)
            }
            createSuccessNotification(project, selectedLocale)
        }
    }

    @Suppress("UnstableApiUsage")
    private suspend fun doReplaceText(project: Project, file: PsiFile?, element: ParadoxLocalisationProperty, snippets: ParadoxLocalisationSnippets) {
        if(snippets.newText == snippets.text) return
        writeCommandAction(project, PlsAiBundle.message("intention.localisation.translate.replace.command")) {
            element.setValue(snippets.newText)
        }
    }

    private fun createSuccessNotification(project: Project, selectedLocale: CwtLocaleConfig) {
        val content = PlsAiBundle.message("intention.replaceLocalisationWithAiTranslation.notification.0", selectedLocale)
        createNotification(content, NotificationType.INFORMATION).notify(project)
    }

    private fun createSuccessWithWarningsNotification(project: Project, selectedLocale: CwtLocaleConfig) {
        val content = PlsAiBundle.message("intention.replaceLocalisationWithAiTranslation.notification.2", selectedLocale)
        createNotification(content, NotificationType.WARNING).notify(project)
    }

    private fun createFailedNotification(project: Project, selectedLocale: CwtLocaleConfig, error: Throwable) {
        thisLogger().warn(error)

        val errorMessage = PlsAiManager.getOptimizedErrorMessage(error)
        val errorDetails = errorMessage?.let { PlsAiBundle.message("intention.localisation.error", it) }.orEmpty()
        val content = PlsAiBundle.message("intention.replaceLocalisationWithAiTranslation.notification.1", selectedLocale) + errorDetails
        createNotification(content, NotificationType.WARNING).notify(project)
    }
}
