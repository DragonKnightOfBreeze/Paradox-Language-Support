package icu.windea.pls.cwt.editor.worldSelection

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtPsiService

// org.jetbrains.kotlin.idea.editor.wordSelection.KotlinCodeBlockSelectioner

class CwtBlockWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !== CwtLanguage) return false
        return findElement(e) != null
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? {
        val element = findElement(e) ?: return null
        val result = mutableListOf<TextRange>()
        selectForBlock(element, cursorOffset, result)
        return result
    }

    private fun findElement(element: PsiElement): CwtBlock? {
        return CwtPsiService.findBlockFromSelfOrBraces(element)
    }

    @Suppress("UNUSED_PARAMETER")
    private fun selectForBlock(element: CwtBlock, cursorOffset: Int, result: MutableList<TextRange>) {
        // add this
        result.add(element.textRange)
    }
}
