package icu.windea.pls.lang.psi

import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.findParentInFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.children
import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.core.collections.WalkingSequenceOptions
import icu.windea.pls.core.collections.forward
import icu.windea.pls.core.findElementAt
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.csv.psi.getColumnIndex
import icu.windea.pls.lang.resolve.ParadoxInlineService
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition

@Suppress("unused")
object ParadoxPsiSequenceBuilder {
    // region Paradox Script

    fun members(element: ParadoxScriptMemberContainer): WalkingSequence<ParadoxScriptMember> {
        val options = WalkingSequenceOptions()
        val delegate = builderMembers(element, options)
        return WalkingSequence(options, delegate)
    }

    private fun builderMembers(element: ParadoxScriptMemberContainer, options: WalkingSequenceOptions): Sequence<ParadoxScriptMember> {
        val nextElement = if (element is ParadoxScriptFile) element.block else element
        if (nextElement == null) return emptySequence()
        return sequence {
            yieldMembers(nextElement, options)
        }
    }

    private suspend fun SequenceScope<ParadoxScriptMember>.yieldMembers(element: ParadoxScriptMemberContainer, options: WalkingSequenceOptions) {
        element.children(options.forward).forEach { child ->
            when (child) {
                is ParadoxScriptMember -> yieldMember(child, options)
                is ParadoxScriptParameterCondition -> if (options.conditional) yieldMembers(child, options)
            }
        }
    }

    private suspend fun SequenceScope<ParadoxScriptMember>.yieldMember(element: ParadoxScriptMember, options: WalkingSequenceOptions) {
        yield(element)
        if (options.inline) yieldInlineMember(element, options)
    }

    private suspend fun SequenceScope<ParadoxScriptMember>.yieldInlineMember(element: ParadoxScriptMember, options: WalkingSequenceOptions) {
        val inlined = ParadoxInlineService.getInlinedElement(element) ?: return
        if (inlined is ParadoxScriptFile) {
            val nextElement = inlined.block
            if (nextElement == null) return
            yieldMembers(nextElement, options)
            return
        }
        yieldMember(inlined, options)
    }

    // endregion

    // region Paradox Localisation

    fun localisations(file: PsiFile): WalkingSequence<ParadoxLocalisationProperty> {
        val options = WalkingSequenceOptions()
        val delegate = buildLocalisations(file, options)
        return WalkingSequence(options, delegate)
    }

    fun localisations(propertyList: ParadoxLocalisationPropertyList): WalkingSequence<ParadoxLocalisationProperty> {
        val options = WalkingSequenceOptions()
        val delegate = buildLocalisations(propertyList, options)
        return WalkingSequence(options, delegate)
    }

    fun selectedLocalisations(editor: Editor, file: PsiFile): WalkingSequence<ParadoxLocalisationProperty> {
        val options = WalkingSequenceOptions()
        val delegate = buildSelectedLocalisations(file, editor, options)
        return WalkingSequence(options, delegate)
    }

    private fun buildLocalisations(file: PsiFile, options: WalkingSequenceOptions): Sequence<ParadoxLocalisationProperty> {
        if (file !is ParadoxLocalisationFile) return emptySequence()
        return sequence {
            file.children(options.forward).filterIsInstance<ParadoxLocalisationPropertyList>().forEach { propertyList ->
                propertyList.children(options.forward).filterIsInstance<ParadoxLocalisationProperty>().forEach { yield(it) }
            }
        }
    }

    private fun buildLocalisations(propertyList: ParadoxLocalisationPropertyList, options: WalkingSequenceOptions): Sequence<ParadoxLocalisationProperty> {
        return sequence {
            propertyList.children(options.forward).filterIsInstance<ParadoxLocalisationProperty>().forEach { yield(it) }
        }
    }

    private fun buildSelectedLocalisations(file: PsiFile, editor: Editor, options: WalkingSequenceOptions): Sequence<ParadoxLocalisationProperty> {
        if (file !is ParadoxLocalisationFile) return emptySequence()
        return sequence {
            val locale = file.findElementAt(editor.caretModel.offset) { it.parentOfType<ParadoxLocalisationLocale>(withSelf = true) }
            if (locale != null) {
                yieldSelectedOf(locale, options)
            } else {
                val selectionStart = editor.selectionModel.selectionStart
                val selectionEnd = editor.selectionModel.selectionEnd
                yieldSelectedBetween(file, options, selectionStart, selectionEnd)
            }
        }
    }

    private suspend fun SequenceScope<ParadoxLocalisationProperty>.yieldSelectedOf(locale: ParadoxLocalisationLocale, options: WalkingSequenceOptions) {
        val propertyList = locale.parent?.castOrNull<ParadoxLocalisationPropertyList>() ?: return
        propertyList.children(options.forward).filterIsInstance<ParadoxLocalisationProperty>().forEach { yield(it) }
    }

    private suspend fun SequenceScope<ParadoxLocalisationProperty>.yieldSelectedBetween(file: PsiFile, options: WalkingSequenceOptions, start: Int, end: Int) {
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

    // endregion

    // region Paradox Csv

    /**
     * 包含选取范围涉及到的所有行。
     */
    fun selectedRows(editor: Editor, file: PsiFile): WalkingSequence<ParadoxCsvRow> {
        val options = WalkingSequenceOptions()
        val delegate = buildSelectedRows(file, editor, options)
        return WalkingSequence(options, delegate)
    }

    private fun buildSelectedRows(file: PsiFile, editor: Editor, options: WalkingSequenceOptions): Sequence<ParadoxCsvRow> {
        if (file !is ParadoxCsvFile) return emptySequence()
        return sequence {
            val set = mutableSetOf<ParadoxCsvRow>()
            val allCarets = editor.caretModel.allCarets.let { if (options.forward) it else it.reversed() }
            for (caret in allCarets) {
                val startRow = yieldStartRow(file, options, caret, set)
                yieldEndRow(file, options, caret, startRow, set)
            }
        }
    }

    private suspend fun SequenceScope<ParadoxCsvRow>.yieldStartRow(file: ParadoxCsvFile, options: WalkingSequenceOptions, caret: Caret, set: MutableSet<ParadoxCsvRow>): ParadoxCsvRow? {
        val offset = if (options.forward) caret.selectionStart else caret.selectionEnd
        val row = file.findElementAt(offset) { it.parentOfType<ParadoxCsvRow>(withSelf = true) }
        if (row == null) return null
        if (set.add(row)) yield(row)
        return row
    }

    private suspend fun SequenceScope<ParadoxCsvRow>.yieldEndRow(file: ParadoxCsvFile, options: WalkingSequenceOptions, caret: Caret, previous: ParadoxCsvRow?, set: MutableSet<ParadoxCsvRow>): ParadoxCsvRow? {
        if (caret.selectionStart == caret.selectionEnd) return null
        val forward = options.forward
        val offset = if (forward) caret.selectionEnd else caret.selectionStart
        val row = file.findElementAt(offset) { it.parentOfType<ParadoxCsvRow>(withSelf = true) }?.takeIf { it != previous }
        if (row == null) return null
        val rowsBetween = previous?.siblings(forward = forward, withSelf = false)?.filterIsInstance<ParadoxCsvRow>()?.takeWhile { it != row }
        rowsBetween?.forEach {
            if (set.add(it)) yield(it)
        }
        if (set.add(row)) yield(row)
        return row
    }

    /**
     * 包含选取范围涉及到的，索引在选取开始与选取结束各自对应的列的索引区间中的所有列。
     */
    fun selectedColumns(editor: Editor, file: PsiFile): WalkingSequence<ParadoxCsvColumn> {
        val options = WalkingSequenceOptions()
        val delegate = buildSelectedColumns(file, editor, options)
        return WalkingSequence(options, delegate)
    }

    private fun buildSelectedColumns(file: PsiFile, editor: Editor, options: WalkingSequenceOptions): Sequence<ParadoxCsvColumn> {
        if (file !is ParadoxCsvFile) return emptySequence()
        return sequence {
            val set = mutableSetOf<ParadoxCsvColumn>()
            val allCarets = editor.caretModel.allCarets.let { if (options.forward) it else it.reversed() }
            for (caret in allCarets) {
                val startColumn = yieldStartColumn(file, options, caret, set)
                yieldEndColumn(file, options, caret, startColumn, set)
            }
        }
    }

    private suspend fun SequenceScope<ParadoxCsvColumn>.yieldStartColumn(file: ParadoxCsvFile, options: WalkingSequenceOptions, caret: Caret, set: MutableSet<ParadoxCsvColumn>): ParadoxCsvColumn? {
        val offset = if (options.forward) caret.selectionStart else caret.selectionEnd
        val column = file.findElementAt(offset) { it.parentOfType<ParadoxCsvColumn>(withSelf = true) }
        if (column == null) return null
        if (set.add(column)) yield(column)
        return column
    }

    private suspend fun SequenceScope<ParadoxCsvColumn>.yieldEndColumn(file: ParadoxCsvFile, options: WalkingSequenceOptions, caret: Caret, previous: ParadoxCsvColumn?, set: MutableSet<ParadoxCsvColumn>): ParadoxCsvColumn? {
        if (caret.selectionStart == caret.selectionEnd) return null
        val forward = options.forward
        val offset = if (forward) caret.selectionEnd else caret.selectionStart
        val column = file.findElementAt(offset) { it.parentOfType<ParadoxCsvColumn>(withSelf = true) }?.takeIf { it != previous }
        if (column == null) return null
        val firstRow = previous?.parent
        val lastRow = column.parent
        if (firstRow != null && lastRow != null) {
            if (firstRow == lastRow) {
                val columnsBetween = previous.siblings(forward = forward, withSelf = false).filterIsInstance<ParadoxCsvColumn>().takeWhile { it != column }
                columnsBetween.forEach {
                    if (set.add(it)) yield(it)
                }
            } else {
                val rows = firstRow.siblings(forward = forward, withSelf = false).filterIsInstance<ParadoxCsvRow>().takeWhile { it != lastRow }
                val startIndex = previous.getColumnIndex()
                val endIndex = column.getColumnIndex()
                val columnsBetween = rows.flatMap { row0 ->
                    when (row0) {
                        firstRow -> previous.siblings(forward = forward, withSelf = false)
                            .filterIsInstance<ParadoxCsvColumn>().takeWhile { it.getColumnIndex() <= endIndex }
                        lastRow -> column.siblings(forward = !forward, withSelf = false)
                            .filterIsInstance<ParadoxCsvColumn>().takeWhile { it.getColumnIndex() >= startIndex }.toList().reversed().asSequence()
                        else -> row0.children(forward = forward)
                            .filterIsInstance<ParadoxCsvColumn>().toList().subList(startIndex, endIndex).asSequence()
                    }
                }
                columnsBetween.forEach {
                    if (set.add(it)) yield(it)
                }
            }
        }
        if (set.add(column)) yield(column)
        return column
    }

    // endregion
}
