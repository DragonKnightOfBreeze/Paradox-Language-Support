package icu.windea.pls.csv.editor

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactory
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.intellij.psi.util.findParentOfType
import com.intellij.util.Consumer
import icu.windea.pls.core.findElementAt
import icu.windea.pls.core.forEachChild
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.csv.psi.getHeaderColumn

/**
 * 用于基于上下文进行高亮。
 *
 * * 当光标位置是列时，高亮对应的头列。
 * * 当光标位置是分隔符时，高亮同一行的所有分隔符。
 * * 当光标位置是列时，如果其中的表达式可以解析引用，高亮当前列。
 */
class ParadoxCsvHighlightUsagesHandlerFactory : HighlightUsagesHandlerFactory, DumbAware {
    override fun createHighlightUsagesHandler(editor: Editor, file: PsiFile): HighlightUsagesHandlerBase<*>? {
        val targets = mutableListOf<PsiElement>()
        addTargetsForSeparator(file, editor, targets)
        addTargetsForColumn(file, editor, targets)
        if (targets.isEmpty()) return null
        return object : HighlightUsagesHandlerBase<PsiElement>(editor, file) {
            override fun getTargets() = targets

            override fun selectTargets(targets: List<PsiElement>, selectionConsumer: Consumer<in List<PsiElement>>) = selectionConsumer.consume(targets)

            override fun computeUsages(targets: List<PsiElement>) {
                val occurrences = mutableListOf<PsiElement>()
                addOccurrencesForSeparatorInSameRow(targets, occurrences)
                addOccurrencesForRelatedColumnInHeader(targets, occurrences)
                addOccurrencesForReferenceColumn(targets, occurrences)
                for (occurrence in occurrences) {
                    val range = when (occurrence) {
                        is ParadoxCsvColumn -> occurrence.textRange.unquote(occurrence.text)
                        else -> occurrence.textRange
                    }
                    myReadUsages.add(range)
                }
            }
        }
    }

    private fun addTargetsForSeparator(file: PsiFile, editor: Editor, targets: MutableList<PsiElement>) {
        val target = file.findElementAt(editor.caretModel.offset) { e -> e.takeIf { it.elementType == ParadoxCsvElementTypes.SEPARATOR } }
        if (target == null) return
        targets += target
    }

    private fun addTargetsForColumn(file: PsiFile, editor: Editor, targets: MutableList<PsiElement>) {
        val target = file.findElementAt(editor.caretModel.offset) { e -> e.findParentOfType<ParadoxCsvColumn>() }
        if (target !is ParadoxCsvColumn) return
        targets += target
    }

    fun addOccurrencesForSeparatorInSameRow(targets: List<PsiElement>, occurrences: MutableList<PsiElement>) {
        for (target in targets) {
            if (target.elementType != ParadoxCsvElementTypes.SEPARATOR) continue
            val container = target.parent?.takeIf { it is ParadoxCsvRow } ?: continue
            container.forEachChild {
                if (it.elementType == ParadoxCsvElementTypes.SEPARATOR) {
                    occurrences += it
                }
            }
        }
    }

    fun addOccurrencesForRelatedColumnInHeader(targets: List<PsiElement>, occurrences: MutableList<PsiElement>) {
        for (target in targets) {
            if (target !is ParadoxCsvColumn) continue
            val headerColumn = target.getHeaderColumn()
            if (headerColumn == null) continue
            occurrences += headerColumn
        }
    }

    fun addOccurrencesForReferenceColumn(targets: List<PsiElement>, occurrences: MutableList<PsiElement>) {
        for (target in targets) {
            if (target !is ParadoxCsvColumn) continue
            if (DumbService.isDumb(target.project)) continue
            if (target.references.all { it.resolveFirst() == null }) continue
            occurrences += target
        }
    }
}
