package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.EditorModificationUtil

class AddCharInsertHandler<T : LookupElement>(private val char: Char) : InsertHandler<T> {
    override fun handleInsert(context: InsertionContext, item: T) {
        // 按照当前的字符来决定是要插入指定的字符，还是什么都不做
        val editor = context.editor
        val caretOffset = editor.caretModel.offset
        val charsSequence = editor.document.charsSequence
        val c = charsSequence.get(caretOffset)
        when (c) {
            char -> {}
            else -> EditorModificationUtil.insertStringAtCaret(editor, char.toString(), false, true, 1)
        }
    }
}
