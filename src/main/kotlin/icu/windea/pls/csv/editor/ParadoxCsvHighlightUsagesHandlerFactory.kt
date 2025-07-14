package icu.windea.pls.csv.editor

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.psi.*

/**
 * 用于基于上下文进行高亮。
 *
 * * 当光标位置是列时，高亮对应的头列。
 * * 当光标位置是分隔符时，高亮同一行的所有分隔符。
 */
class ParadoxCsvHighlightUsagesHandlerFactory : HighlightUsagesHandlerFactory, DumbAware {
    override fun createHighlightUsagesHandler(editor: Editor, file: PsiFile): HighlightUsagesHandlerBase<*>? {
        val targets = mutableListOf<PsiElement>()
        findTargetForRelatedColumnInHeader(file, editor)?.let { targets += it }
        findTargetForSeparatorsInSameRow(file, editor)?.let { targets += it }
        if (targets.isEmpty()) return null
        return object : HighlightUsagesHandlerBase<PsiElement>(editor, file) {
            override fun getTargets() = targets

            override fun selectTargets(targets: List<PsiElement>, selectionConsumer: Consumer<in List<PsiElement>>) = selectionConsumer.consume(targets)

            override fun computeUsages(targets: List<PsiElement>) {
                addOccurrences(targets) { addOccurrence(it) }
            }
        }
    }

    private fun findTargetForRelatedColumnInHeader(file: PsiFile, editor: Editor): ParadoxCsvColumn? {
        val target = file.findElementAt(editor.caretModel.offset) { e -> e.findParentOfType<ParadoxCsvColumn>() }
        if (target !is ParadoxCsvColumn) return null
        if (target.isHeaderColumn()) return null
        return target
    }

    private fun findTargetForSeparatorsInSameRow(file: PsiFile, editor: Editor): PsiElement? {
        val target = file.findElementAt(editor.caretModel.offset) { e -> e.takeIf { it.elementType == ParadoxCsvElementTypes.SEPARATOR } }
        if (target == null) return null
        return target
    }

    private fun addOccurrences(targets: List<PsiElement>, addOccurrence: (PsiElement) -> Unit) {
        targets.forEach { target ->
            if (target.elementType == ParadoxCsvElementTypes.SEPARATOR) {
                val container = target.parent?.takeIf { it is ParadoxCsvHeader || it is ParadoxCsvRow } ?: return
                container.forEachChild {
                    if (it.elementType == ParadoxCsvElementTypes.SEPARATOR) {
                        addOccurrence(it)
                    }
                }
            } else if (target is ParadoxCsvColumn) {
                val headerColumn = target.getHeaderColumn()
                if (headerColumn != null) {
                    addOccurrence(headerColumn)
                }
            }
        }
    }
}
