@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.util.manipulators

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.readAction
import com.intellij.openapi.command.writeCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts.Command
import com.intellij.platform.ide.progress.withBackgroundProgress
import com.intellij.psi.PsiFile
import com.intellij.psi.util.findParentInFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.findElementAt
import icu.windea.pls.integrations.translation.PlsTranslationManager
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.locale
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.dataFlow.ParadoxLocalisationSequence
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import icu.windea.pls.lang.util.dataFlow.ParadoxDataFlowOptions.Localisation as LocalisationOptions

object ParadoxLocalisationManipulator {
    @Suppress("unused")
    fun buildEmptySequence(): ParadoxLocalisationSequence {
        return ParadoxLocalisationSequence(emptySequence(), LocalisationOptions())
    }

    fun buildSequence(file: PsiFile): ParadoxLocalisationSequence {
        val options = LocalisationOptions()
        val delegate = doBuildSequence(file, options)
        return ParadoxLocalisationSequence(delegate, options)
    }

    @Suppress("unused")
    fun buildSequence(propertyList: ParadoxLocalisationPropertyList): ParadoxLocalisationSequence {
        val options = LocalisationOptions()
        val delegate = doBuildSequence(propertyList, options)
        return ParadoxLocalisationSequence(delegate, options)
    }

    fun buildSelectedSequence(editor: Editor, file: PsiFile): ParadoxLocalisationSequence {
        val options = LocalisationOptions()
        val delegate = doBuildSelectedSequence(file, editor, options)
        return ParadoxLocalisationSequence(delegate, options)
    }

    private fun doBuildSequence(file: PsiFile, options: LocalisationOptions): Sequence<ParadoxLocalisationProperty> {
        if (file !is ParadoxLocalisationFile) return emptySequence()
        return sequence {
            file.children(options.forward).filterIsInstance<ParadoxLocalisationPropertyList>().forEach { propertyList ->
                propertyList.children(options.forward).filterIsInstance<ParadoxLocalisationProperty>().forEach { yield(it) }
            }
        }
    }

    private fun doBuildSequence(propertyList: ParadoxLocalisationPropertyList, options: LocalisationOptions): Sequence<ParadoxLocalisationProperty> {
        return sequence {
            propertyList.children(options.forward).filterIsInstance<ParadoxLocalisationProperty>().forEach { yield(it) }
        }
    }

    private fun doBuildSelectedSequence(file: PsiFile, editor: Editor, options: LocalisationOptions): Sequence<ParadoxLocalisationProperty> {
        if (file !is ParadoxLocalisationFile) return emptySequence()
        return sequence {
            val locale = file.findElementAt(editor.caretModel.offset) { it.parentOfType<ParadoxLocalisationLocale>(withSelf = true) }
            if (locale != null) {
                doYieldSelectedOf(locale, options)
            } else {
                val selectionStart = editor.selectionModel.selectionStart
                val selectionEnd = editor.selectionModel.selectionEnd
                doYieldSelectedBetween(file, selectionStart, selectionEnd, options)
            }
        }
    }

    private suspend fun SequenceScope<ParadoxLocalisationProperty>.doYieldSelectedOf(locale: ParadoxLocalisationLocale, options: LocalisationOptions) {
        val propertyList = locale.parent?.castOrNull<ParadoxLocalisationPropertyList>() ?: return
        propertyList.children(options.forward).filterIsInstance<ParadoxLocalisationProperty>().forEach { yield(it) }
    }

    private suspend fun SequenceScope<ParadoxLocalisationProperty>.doYieldSelectedBetween(file: PsiFile, start: Int, end: Int, options: LocalisationOptions) {
        if (start == end) {
            val originalElement = file.findElementAt(start)
            val element = originalElement?.parentOfType<ParadoxLocalisationProperty>() ?: return
            yield(element)
            return
        }
        val originalStartElement = file.findElementAt(start) ?: return
        val originalEndElement = file.findElementAt(end)
        val startElement = originalStartElement.findParentInFile(true) { it.parent is ParadoxLocalisationPropertyList }
        val endElement = originalEndElement?.findParentInFile(true) { it.parent is ParadoxLocalisationPropertyList }
        if (startElement == null && endElement == null) return
        if (startElement == endElement) {
            if (startElement is ParadoxLocalisationProperty) yield(startElement)
            return
        }
        val listElement = startElement?.parent ?: endElement?.parent ?: return
        val firstElement = startElement ?: listElement.firstChild ?: return
        val forwardFirst = if (endElement == null) true else firstElement.startOffset <= endElement.startOffset
        val forward = if (options.forward) forwardFirst else !forwardFirst
        firstElement.siblings(forward = forward, withSelf = true).forEach {
            if (it is ParadoxLocalisationProperty) yield(it)
            if (it == endElement) return
        }
    }

    suspend fun searchTextFromLocale(context: ParadoxLocalisationContext, project: Project, locale: CwtLocaleConfig) {
        val newText = readAction {
            val selector = selector(project, context.element).localisation().contextSensitive().locale(locale)
            val e = ParadoxLocalisationSearch.search(context.key, selector).find()
            e?.value
        }
        if (newText == null) return
        context.newText = newText
    }

    suspend fun handleTextWithTranslation(context: ParadoxLocalisationContext, sourceLocale: CwtLocaleConfig, targetLocale: CwtLocaleConfig) {
        val newText = suspendCancellableCoroutine { continuation ->
            CoroutineScope(continuation.context).launch {
                PlsTranslationManager.translate(context.newText, sourceLocale, targetLocale) { translated, e ->
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
        return contexts.joinToString("\n") { context -> "${context.prefix}\"${context.newText}\"" }
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
