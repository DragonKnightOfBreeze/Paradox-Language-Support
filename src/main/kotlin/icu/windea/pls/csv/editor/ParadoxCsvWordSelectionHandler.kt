package icu.windea.pls.csv.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.csv.*
import icu.windea.pls.csv.psi.*

class ParadoxCsvWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !is ParadoxCsvLanguage) return false
        val element = e.parents(true).find { findElementToSelect(it) } ?: return false
        if (!element.text.isLeftQuoted()) return false
        return true
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? {
        val element = e.parents(true).find { findElementToSelect(it) } ?: return null
        val offset1 = if (element.text.isLeftQuoted()) 1 else return null
        val offset2 = if (element.text.isRightQuoted()) -1 else 0
        val textRange = element.textRange
        if (textRange.isEmpty) return null
        return listOf(TextRange.create(textRange.startOffset + offset1, textRange.endOffset + offset2))
    }

    private fun findElementToSelect(element: PsiElement): Boolean {
        return element is ParadoxCsvColumn
    }
}
