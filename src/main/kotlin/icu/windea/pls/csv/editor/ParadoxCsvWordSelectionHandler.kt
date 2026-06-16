package icu.windea.pls.csv.editor

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.unquote
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement

// com.intellij.json.editor.selection.JsonStringLiteralSelectionHandler

class ParadoxCsvWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !is ParadoxCsvLanguage) return false
        val element = findExpressionElement(e)
        if (element != null) return true
        return false
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? {
        val result = mutableListOf<TextRange>()
        selectExpressionElement(e, result)
        if (result.isEmpty()) return null
        return result
    }

    private fun findExpressionElement(element: PsiElement): ParadoxCsvExpressionElement? {
        return element.parent?.castOrNull()
    }

    private fun selectExpressionElement(e: PsiElement, result: MutableList<TextRange>) {
        val element = findExpressionElement(e) ?: return
        val textRange = element.textRange
        if (textRange.isEmpty) return
        selectUnquoted(element, textRange, result)
    }

    private fun selectUnquoted(element: ParadoxCsvExpressionElement, textRange: TextRange, result: MutableList<TextRange>) {
        val text = element.text
        if (!text.isLeftQuoted()) return
        result += textRange.unquote(text)
    }
}
