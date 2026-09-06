package icu.windea.pls.script.editor.wordSelection

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptPsiService

// org.jetbrains.kotlin.idea.editor.wordSelection.KotlinCodeBlockSelectioner

class ParadoxScriptInlineMathWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !== ParadoxScriptLanguage) return false
        return findElement(e) != null
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? {
        val element = findElement(e) ?: return null
        val result = mutableListOf<TextRange>()
        selectForInlineMath(element, cursorOffset, result)
        return result
    }

    private fun findElement(element: PsiElement): ParadoxScriptInlineMath? {
        return ParadoxScriptPsiService.findInlineMathFromSelfOrBrackets(element)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun selectForInlineMath(element: ParadoxScriptInlineMath, cursorOffset: Int, result: MutableList<TextRange>) {
        // add this
        result.add(element.textRange)
    }
}
