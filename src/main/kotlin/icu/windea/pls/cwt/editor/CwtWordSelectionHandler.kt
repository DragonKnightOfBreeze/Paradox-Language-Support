package icu.windea.pls.cwt.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.parents
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtString

class CwtWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !is CwtLanguage) return false
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
        return element is CwtPropertyKey || element is CwtString
    }
}
