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

    // no additional text ranges here
    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange?>? {
        return super.select(e, editorText, cursorOffset, editor)
    }

    private fun findElement(element: PsiElement): CwtBlock? {
        return CwtPsiService.findBlockFromSelfOrBraces(element)
    }
}
