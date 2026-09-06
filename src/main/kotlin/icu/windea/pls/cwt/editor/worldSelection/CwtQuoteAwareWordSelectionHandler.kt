package icu.windea.pls.cwt.editor.worldSelection

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.isQuoted
import icu.windea.pls.core.psi.PsiQuoteAwareElement
import icu.windea.pls.core.unquote
import icu.windea.pls.cwt.CwtLanguage

// com.intellij.json.editor.selection.JsonStringLiteralSelectionHandler

class CwtQuoteAwareWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !== CwtLanguage) return false
        return findElement(e) != null
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? {
        val element = findElement(e) ?: return null
        val result = mutableListOf<TextRange>()
        selectUnquoted(element, result)
        return result
    }

    private fun findElement(element: PsiElement): PsiQuoteAwareElement? {
        return element.parent?.castOrNull()
    }

    private fun selectUnquoted(element: PsiQuoteAwareElement, result: MutableList<TextRange>) {
        val quotePattern = element.quotePattern
        val text = element.text
        if (!text.isQuoted(quotePattern)) return // left quoted or right quoted
        val textRange = element.textRange
        result.add(textRange.unquote(text, quotePattern))
    }
}
