@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.NlsContexts.*
import com.intellij.platform.ide.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.integrations.translation.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

object ParadoxLocalisationManipulator {
    fun buildSequence(file: PsiFile): Sequence<ParadoxLocalisationProperty> {
        if (file !is ParadoxLocalisationFile) return emptySequence()
        return sequence {
            file.children().filterIsInstance<ParadoxLocalisationPropertyList>().flatMap { buildSequence(it) }
        }
    }

    fun buildSequence(propertyList: ParadoxLocalisationPropertyList): Sequence<ParadoxLocalisationProperty> {
        return propertyList.children().filterIsInstance<ParadoxLocalisationProperty>()
    }

    fun buildSelectedSequence(editor: Editor, file: PsiFile): Sequence<ParadoxLocalisationProperty> {
        if (file !is ParadoxLocalisationFile) return emptySequence()

        val locale = file.findElementAt(editor.caretModel.offset) { it.parentOfType<ParadoxLocalisationLocale>(withSelf = true) }
        if (locale != null) return doBuildSelectedSequenceOf(locale)

        val selectionStart = editor.selectionModel.selectionStart
        val selectionEnd = editor.selectionModel.selectionEnd
        return doBuildSelectedSequenceBetween(file, selectionStart, selectionEnd)
    }

    private fun doBuildSelectedSequenceOf(locale: ParadoxLocalisationLocale): Sequence<ParadoxLocalisationProperty> {
        val propertyList = locale.parent?.castOrNull<ParadoxLocalisationPropertyList>() ?: return emptySequence()
        return buildSequence(propertyList)
    }

    private fun doBuildSelectedSequenceBetween(file: PsiFile, start: Int, end: Int): Sequence<ParadoxLocalisationProperty> {
        if (start == end) {
            val originalElement = file.findElementAt(start)
            val element = originalElement?.parentOfType<ParadoxLocalisationProperty>() ?: return emptySequence()
            return sequenceOf(element)
        }
        val originalStartElement = file.findElementAt(start) ?: return emptySequence()
        val originalEndElement = file.findElementAt(end)
        val startElement = originalStartElement.findParentInFile(true) { it.parent is ParadoxLocalisationPropertyList }
        val endElement = originalEndElement?.findParentInFile(true) { it.parent is ParadoxLocalisationPropertyList }
        if (startElement == null && endElement == null) return emptySequence()
        if (startElement == endElement) {
            if (startElement is ParadoxLocalisationProperty) return sequenceOf(startElement)
            return emptySequence()
        }
        val listElement = startElement?.parent ?: endElement?.parent ?: return emptySequence()
        val firstElement = startElement ?: listElement.firstChild ?: return emptySequence()
        val forward = if (endElement == null) true else firstElement.startOffset <= endElement.startOffset
        return sequence {
            firstElement.siblings(forward = forward, withSelf = true).forEach {
                if (it is ParadoxLocalisationProperty) yield(it)
                if (it == endElement) return@sequence
            }
        }
    }

    fun buildFlow(file: PsiFile): Flow<ParadoxLocalisationProperty> {
        if (file !is ParadoxLocalisationFile) return emptyFlow()

        return flow {
            val c1 = readAction { file.children() }
            c1.filterIsInstance<ParadoxLocalisationPropertyList>().forEach { propertyList ->
                val c2 = readAction { propertyList.children() }
                c2.filterIsInstance<ParadoxLocalisationProperty>().forEach { property ->
                    emit(property)
                }
            }
        }
    }

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

    suspend fun replaceText(context: ParadoxLocalisationContext, project: Project, @Command commandName: String) {
        if (context.newText == context.text) return
        writeCommandAction(project, commandName) {
            // 注意这里 context.element 可能已经不合法
            context.element?.setValue(context.newText)
        }
    }

    fun joinText(contexts: List<ParadoxLocalisationContext>): String {
        return contexts.joinToString("\n") { context -> context.joinWithNewText() }
    }

    fun createRevertAction(contexts: List<ParadoxLocalisationContext>): AnAction {
        return object : AnAction(PlsBundle.message("manipulation.localisation.revert")) {
            override fun actionPerformed(e: AnActionEvent) {
                val project = e.project ?: return
                val coroutineScope = PlsFacade.getCoroutineScope(project)
                coroutineScope.launch {
                    withBackgroundProgress(project, PlsBundle.message("manipulation.localisation.revert.progress.title")) {
                        writeCommandAction(project, PlsBundle.message("manipulation.localisation.revert.command")) {
                            for (context in contexts) {
                                if (context.text == context.newText) continue
                                // 注意这里 context.element 可能已经不合法
                                context.element?.setValue(context.text)
                            }
                        }
                    }
                }
            }
        }
    }

    fun createReapplyAction(contexts: List<ParadoxLocalisationContext>): AnAction {
        return object : AnAction(PlsBundle.message("manipulation.localisation.reapply")) {
            override fun actionPerformed(e: AnActionEvent) {
                val project = e.project ?: return
                val coroutineScope = PlsFacade.getCoroutineScope(project)
                coroutineScope.launch {
                    withBackgroundProgress(project, PlsBundle.message("manipulation.localisation.reapply.progress.title")) {
                        writeCommandAction(project, PlsBundle.message("manipulation.localisation.reapply.command")) {
                            for (context in contexts) {
                                if (context.text == context.newText) continue
                                // 注意这里 context.element 可能已经不合法
                                context.element?.setValue(context.newText)
                            }
                        }
                    }
                }
            }
        }
    }
}
