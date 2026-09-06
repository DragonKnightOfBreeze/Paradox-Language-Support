package icu.windea.pls.script.editor.wordSelection

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptPsiService

// org.jetbrains.kotlin.idea.editor.wordSelection.KotlinCodeBlockSelectioner

class ParadoxScriptBlockWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !== ParadoxScriptLanguage) return false
        return findElement(e) != null
    }

    // no additional text ranges here
    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange?>? {
        return super.select(e, editorText, cursorOffset, editor)
    }

    private fun findElement(element: PsiElement): ParadoxScriptBlock? {
        return ParadoxScriptPsiService.findBlockFromSelfOrBraces(element)
    }
}
