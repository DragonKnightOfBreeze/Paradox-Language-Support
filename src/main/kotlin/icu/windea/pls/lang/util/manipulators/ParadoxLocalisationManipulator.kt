@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.application.*
import com.intellij.openapi.command.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.NlsContexts.*
import icu.windea.pls.ai.requests.*
import icu.windea.pls.ai.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.integrations.translation.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

object ParadoxLocalisationManipulator {
    suspend fun handleTextFromLocale(context: ParadoxLocalisationContext, project: Project, selectedLocale: CwtLocaleConfig) {
        val newText = readAction {
            val selector = selector(project, context.element).localisation().contextSensitive().locale(selectedLocale)
            val e = ParadoxLocalisationSearch.search(context.key, selector).find() ?: return@readAction null
            e.value
        }
        if (newText == null) return
        context.newText = newText
    }

    suspend fun handleTextWithTranslation(context: ParadoxLocalisationContext, selectedLocale: CwtLocaleConfig) {
        val sourceLocale = selectLocale(context.element)
        val newText = suspendCancellableCoroutine { continuation ->
            CoroutineScope(continuation.context).launch {
                PlsTranslationManager.translate(context.text, sourceLocale, selectedLocale) { translated, e ->
                    if (e != null) {
                        continuation.resumeWithException(e)
                    } else {
                        continuation.resume(translated)
                    }
                }
            }
        }
        if (newText == null) return
        context.newText = newText
    }

    suspend fun handleTextWithAiTranslation(request: PlsAiTranslateLocalisationsRequest, callback: suspend (ParadoxLocalisationResult) -> Unit) {
        val aiService = PlsAiManager.getTranslateLocalisationService()
        val resultFlow = aiService.translate(request)
        aiService.checkResultFlow(resultFlow)
        resultFlow.collect { data ->
            val context = request.inputContexts[request.index]
            aiService.checkOutputData(context, data)
            context.newText = data.text
            request.index++

            callback(data)
        }
    }

    suspend fun handleTextWithAiPolishing(request: PlsAiPolishLocalisationsRequest, callback: suspend (ParadoxLocalisationResult) -> Unit) {
        val aiService = PlsAiManager.getPolishLocalisationService()
        val resultFlow = aiService.polish(request)
        aiService.checkResultFlow(resultFlow)
        resultFlow.collect { data ->
            val context = request.inputContexts[request.index]
            aiService.checkOutputData(context, data)
            context.newText = data.text
            request.index++

            callback(data)
        }
    }

    suspend fun replaceText(context: ParadoxLocalisationContext, project: Project, @Command commandName: String) {
        if (context.newText == context.text) return
        writeCommandAction(project, commandName) {
            context.element.setValue(context.newText)
        }
    }

    fun joinText(contexts: List<ParadoxLocalisationContext>): String {
        return contexts.joinToString("\n") { context -> context.joinWithNewText() }
    }
}
