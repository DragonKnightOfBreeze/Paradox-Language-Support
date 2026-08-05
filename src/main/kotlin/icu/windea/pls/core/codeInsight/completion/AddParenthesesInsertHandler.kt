package icu.windea.pls.core.codeInsight.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.EditorModificationUtil

class AddParenthesesInsertHandler<T : LookupElement> : InsertHandler<T> {
    override fun handleInsert(context: InsertionContext, item: T) {
        // 按照当前的字符来决定是要插入左括号和右括号，还是仅插入左括号，还是什么都不做
        val editor = context.editor
        val caretOffset = editor.caretModel.offset
        val charsSequence = editor.document.charsSequence
        val c = charsSequence.get(caretOffset)
        when (c) {
            '(' -> {}
            ')' -> EditorModificationUtil.insertStringAtCaret(editor, "(", false, true, 1)
            else -> EditorModificationUtil.insertStringAtCaret(editor, "()", false, true, 1)
        }
    }
}
