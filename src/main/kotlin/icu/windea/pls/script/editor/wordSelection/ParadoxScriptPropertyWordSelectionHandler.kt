package icu.windea.pls.script.editor.wordSelection

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.core.castOrNull
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptProperty

// org.jetbrains.kotlin.idea.editor.wordSelection.KotlinCodeBlockSelectioner

class ParadoxScriptPropertyWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !== ParadoxScriptLanguage) return false
        return findElement(e) != null
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? {
        val element = findElement(e) ?: return null
        val result = mutableListOf<TextRange>()
        selectForProperty(element, cursorOffset, result)
        return result
    }

    private fun findElement(element: PsiElement): ParadoxScriptProperty? {
        return element.parent?.castOrNull()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun selectForProperty(element: ParadoxScriptProperty, cursorOffset: Int, result: MutableList<TextRange>) {
        // add this
        result.add(element.textRange)
    }
}
