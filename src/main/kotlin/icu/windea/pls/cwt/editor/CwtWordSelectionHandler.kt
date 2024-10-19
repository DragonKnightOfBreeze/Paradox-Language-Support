package icu.windea.pls.cwt.editor

import com.intellij.codeInsight.editorActions.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.psi.*

class CwtWordSelectionHandler : ExtendWordSelectionHandlerBase() {
    override fun canSelect(e: PsiElement): Boolean {
        if (e.language != CwtLanguage) return false
        val element = e.parents(true).find { it is CwtPropertyKey || it is CwtString } ?: return false
        if (!element.text.isLeftQuoted()) return false
        return true
    }

    override fun select(e: PsiElement, editorText: CharSequence, cursorOffset: Int, editor: Editor): List<TextRange>? {
        val element = e.parents(true).find { it is CwtPropertyKey || it is CwtString } ?: return null
        val offset1 = if (element.text.isLeftQuoted()) 1 else return null
        val offset2 = if (element.text.isRightQuoted()) -1 else 0
        val textRange = element.textRange
        if (textRange.isEmpty) return null
        return listOf(TextRange.create(textRange.startOffset + offset1, textRange.endOffset + offset2))
    }
}
