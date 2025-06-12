package icu.windea.pls.ai.intentions

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
import icu.windea.pls.ai.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.services.*
import icu.windea.pls.ai.settings.*
import icu.windea.pls.ai.util.PlsAiManager
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.intentions.localisation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import java.awt.datatransfer.*
import java.util.concurrent.atomic.*

/**
 * （基于AI）复制翻译后的本地化（光标位置对应的本地化，或者光标选取范围涉及到的所有本地化）到剪贴板。
 *
 * 复制的文本格式为：`KEY:0 "TEXT"`
 */
class CopyLocalisationWithAiTranslationIntention : CopyLocalisationIntentionBase() {
    override fun getFamilyName() = PlsAiBundle.message("intention.copyLocalisationWithAiTranslation")

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        return super.isAvailable(project, editor, file) && PlsAiManager.isAvailable()
    }

    @Suppress("UnstableApiUsage")
    override suspend fun doHandle(project: Project, file: PsiFile?, elements: List<ParadoxLocalisationProperty>, selectedLocale: CwtLocaleConfig?) {
        if (selectedLocale == null) return
        withBackgroundProgress(project, PlsAiBundle.message("intention.copyLocalisationWithAiTranslation.progress.title", selectedLocale)) action@{
            val elementsAndSnippets = elements.map { it to readAction { ParadoxLocalisationSnippets.from(it) } }
            val elementsAndSnippetsToHandle = elementsAndSnippets.filter { (_, snippets) -> snippets.text.isNotBlank() }
            val errorRef = AtomicReference<Throwable>()
            var withWarnings = false
            val elementsAndSnippetsChunked = elementsAndSnippetsToHandle.chunked(PlsAiConstantSettings.maxLineLength)

            reportProgress(elementsAndSnippetsChunked.size) { reporter ->
                elementsAndSnippetsChunked.forEachConcurrent { list ->
                    val inputText = list.joinToString("\n") { (_, snippets) -> snippets.join() }
                    var i = 0
                    reporter.itemStep(PlsAiBundle.message("intention.localisation.translate.progress.step")) {
                        val request = PlsAiTranslateLocalisationsRequest(elements, inputText, selectedLocale, file, project)
                        val resultFlow = PlsAiTranslateLocalisationService.translate(request) ?: return@itemStep
                        runCatchingCancelable {
                            resultFlow.collect { data ->
                                val (_, currentSnippets) = list[i]
                                if (currentSnippets.key != data.key) { //不期望的结果，直接报错，中断收集
                                    throw IllegalStateException()
                                }
                                currentSnippets.newText = data.text
                                i++
                            }
                        }.onFailure { errorRef.set(it) }.getOrNull()
                    }
                    if (i != elementsAndSnippetsChunked.size) { //不期望的结果，但是不报错（假定这是因为AI仅翻译了部分条目导致的）
                        withWarnings = true
                    }
                }
            }

            if (errorRef.get() != null) {
                return@action createFailedNotification(project, selectedLocale, errorRef.get())
            }

            val textToCopy = elementsAndSnippets.joinToString("\n") { (_, snippets) -> snippets.joinWithNewText() }
            CopyPasteManager.getInstance().setContents(StringSelection(textToCopy))
            if (withWarnings) {
                return@action createSuccessWithWarningsNotification(project, selectedLocale)
            }
            createSuccessNotification(project, selectedLocale)
        }
    }

    private fun createSuccessNotification(project: Project, selectedLocale: CwtLocaleConfig) {
        val content = PlsAiBundle.message("intention.copyLocalisationWithAiTranslation.notification.0", selectedLocale)
        createNotification(content, NotificationType.INFORMATION).notify(project)
    }

    private fun createSuccessWithWarningsNotification(project: Project, selectedLocale: CwtLocaleConfig) {
        val content = PlsAiBundle.message("intention.copyLocalisationWithAiTranslation.notification.2", selectedLocale)
        createNotification(content, NotificationType.WARNING).notify(project)
    }

    private fun createFailedNotification(project: Project, selectedLocale: CwtLocaleConfig, error: Throwable) {
        thisLogger().warn(error)

        val errorDetails = error.message?.let { "<br>$it" }.orEmpty()
        val content = PlsAiBundle.message("intention.copyLocalisationWithAiTranslation.notification.1", selectedLocale) + errorDetails
        createNotification(content, NotificationType.WARNING).notify(project)
    }
}
