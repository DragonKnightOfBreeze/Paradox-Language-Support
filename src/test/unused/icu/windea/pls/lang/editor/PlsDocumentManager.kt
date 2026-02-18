@file:Suppress("unused")

package icu.windea.pls.lang.editor

import com.intellij.openapi.editor.Document
import com.intellij.util.DocumentUtil

object PlsDocumentManager {
    fun isAtLineStart(document: Document, offset: Int, skipWhitespaces: Boolean = false): Boolean {
        if (!skipWhitespaces) return DocumentUtil.isAtLineStart(offset, document)
        val lineStartOffset = DocumentUtil.getLineStartOffset(offset, document)
        val charsSequence = document.charsSequence
        for (i in offset..lineStartOffset) {
            val c = charsSequence[i]
            if (!c.isWhitespace()) {
                return false
            }
        }
        return true
    }

    fun isAtLineEnd(document: Document, offset: Int, skipWhitespaces: Boolean = false): Boolean {
        if (!skipWhitespaces) return DocumentUtil.isAtLineEnd(offset, document)
        val lineEndOffset = DocumentUtil.getLineEndOffset(offset, document)
        val charsSequence = document.charsSequence
        for (i in offset..lineEndOffset) {
            if (i >= charsSequence.length) return true
            val c = charsSequence[i]
            if (!c.isWhitespace()) {
                return false
            }
        }
        return true
    }
}
