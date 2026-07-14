package icu.windea.pls.lang.manipulation

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.findParentInFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.WalkingContext
import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.core.collections.forward
import icu.windea.pls.core.findElementAt
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

object ParadoxLocalisationFileManipulationService {
    fun localisations(file: PsiFile): WalkingSequence<ParadoxLocalisationProperty> {
        val context = WalkingContext()
        val delegate = with(context) { buildLocalisations(file) }
        return WalkingSequence(delegate, context)
    }

    fun localisations(propertyList: ParadoxLocalisationPropertyList): WalkingSequence<ParadoxLocalisationProperty> {
        val context = WalkingContext()
        val delegate = with(context) { buildLocalisations(propertyList) }
        return WalkingSequence(delegate, context)
    }

    fun selectedLocalisations(editor: Editor, file: PsiFile): WalkingSequence<ParadoxLocalisationProperty> {
        val context = WalkingContext()
        val delegate = with(context) { buildSelectedLocalisations(file, editor) }
        return WalkingSequence(delegate, context)
    }

    context(context: WalkingContext)
    private fun buildLocalisations(file: PsiFile): Sequence<ParadoxLocalisationProperty> {
        if (file !is ParadoxLocalisationFile) return emptySequence()
        return sequence {
            file.children(context.forward).filterIsInstance<ParadoxLocalisationPropertyList>().forEach { propertyList ->
                ProgressManager.checkCanceled()
                propertyList.children(context.forward).filterIsInstance<ParadoxLocalisationProperty>().forEach { yield(it) }
            }
        }
    }

    context(context: WalkingContext)
    private fun buildLocalisations(propertyList: ParadoxLocalisationPropertyList): Sequence<ParadoxLocalisationProperty> {
        return sequence {
            propertyList.children(context.forward).filterIsInstance<ParadoxLocalisationProperty>().forEach { yield(it) }
        }
    }

    context(context: WalkingContext)
    private fun buildSelectedLocalisations(file: PsiFile, editor: Editor): Sequence<ParadoxLocalisationProperty> {
        if (file !is ParadoxLocalisationFile) return emptySequence()
        return sequence {
            val locale = file.findElementAt(editor.caretModel.offset) { it.parentOfType<ParadoxLocalisationLocale>(withSelf = true) }
            if (locale != null) {
                yieldSelectedOf(locale)
            } else {
                val selectionStart = editor.selectionModel.selectionStart
                val selectionEnd = editor.selectionModel.selectionEnd
                yieldSelectedBetween(file, selectionStart, selectionEnd)
            }
        }
    }

    context(context: WalkingContext)
    private suspend fun SequenceScope<ParadoxLocalisationProperty>.yieldSelectedOf(locale: ParadoxLocalisationLocale) {
        ProgressManager.checkCanceled()
        val propertyList = locale.parent?.castOrNull<ParadoxLocalisationPropertyList>() ?: return
        propertyList.children(context.forward).filterIsInstance<ParadoxLocalisationProperty>().forEach { yield(it) }
    }

    context(context: WalkingContext)
    private suspend fun SequenceScope<ParadoxLocalisationProperty>.yieldSelectedBetween(file: PsiFile, start: Int, end: Int) {
        ProgressManager.checkCanceled()
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
        val forward = if (context.forward) forwardFirst else !forwardFirst
        firstElement.siblings(forward = forward, withSelf = true).forEach {
            if (it is ParadoxLocalisationProperty) yield(it)
            if (it == endElement) return
        }
    }
}
