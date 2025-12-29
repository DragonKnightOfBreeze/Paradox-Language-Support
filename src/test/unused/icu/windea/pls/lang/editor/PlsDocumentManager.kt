@file:Suppress("unused")

package icu.windea.pls.lang.editor

import com.intellij.openapi.editor.Document
import com.intellij.util.DocumentUtil

object PlsDocumentManager {
    fun Document.isAtLineStart(offset: Int, skipWhitespaces: Boolean = false): Boolean {
        if (!skipWhitespaces) return DocumentUtil.isAtLineStart(offset, this)
        val lineStartOffset = DocumentUtil.getLineStartOffset(offset, this)
        val charsSequence = charsSequence
        for (i in offset..lineStartOffset) {
            val c = charsSequence[i]
            if (!c.isWhitespace()) {
                return false
            }
        }
        return true
    }

    fun Document.isAtLineEnd(offset: Int, skipWhitespaces: Boolean = false): Boolean {
        if (!skipWhitespaces) return DocumentUtil.isAtLineEnd(offset, this)
        val lineEndOffset = DocumentUtil.getLineEndOffset(offset, this)
        val charsSequence = charsSequence
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
