package icu.windea.pls.cwt.editor.worldSelection

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.core.castOrNull
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtOption

// org.jetbrains.kotlin.idea.editor.wordSelection.KotlinCodeBlockSelectioner

class CwtOptionWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !== CwtLanguage) return false
        return findElement(e) != null
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? {
        val element = findElement(e) ?: return null
        val result = mutableListOf<TextRange>()
        selectForProperty(element, cursorOffset, result)
        return result
    }

    private fun findElement(element: PsiElement): CwtOption? {
        return element.parent?.castOrNull()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun selectForProperty(element: CwtOption, cursorOffset: Int, result: MutableList<TextRange>) {
        // add this
        result.add(element.textRange)
    }
}
