package icu.windea.pls.script.editor.wordSelection

import com.intellij.codeInsight.editorActions.ExtendWordSelectionHandlerBase
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.findChild
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptPsiService

// org.jetbrains.kotlin.idea.editor.wordSelection.KotlinCodeBlockSelectioner

class ParadoxScriptConditionalBlockWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language !== ParadoxScriptLanguage) return false
        return findElement(e) != null
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? {
        val element = findElement(e) ?: return null
        val result = mutableListOf<TextRange>()
        selectForConditionalBlock(element, cursorOffset, result)
        return result
    }

    private fun findElement(element: PsiElement): ParadoxScriptConditionalBlock? {
        return ParadoxScriptPsiService.findConditionalBlockFromSelfOrBrackets(element)
    }

    private fun selectForConditionalBlock(element: ParadoxScriptConditionalBlock, cursorOffset: Int, result: MutableList<TextRange>) {
        // add nested
        val nestedStartOffset = element.findChild { it.elementType == NESTED_LEFT_BRACKET }?.startOffset
        val nestedEndOffset = element.findChild { it.elementType == NESTED_RIGHT_BRACKET }?.endOffset
        if (nestedStartOffset != null && nestedEndOffset != null && cursorOffset in nestedStartOffset..nestedEndOffset) {
            result.add(TextRange.create(nestedStartOffset, nestedEndOffset))
        }
        // add this
        result.add(element.textRange)
    }
}
