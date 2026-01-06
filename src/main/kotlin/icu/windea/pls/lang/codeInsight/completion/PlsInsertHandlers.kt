package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.editor.EditorModificationUtil
import icu.windea.pls.lang.codeStyle.PlsCodeStyleUtil

object PlsInsertHandlers {
    fun addParentheses(): InsertHandler<LookupElement> {
        return InsertHandler { c, _ ->
            // 按照当前的字符来决定是要插入左括号和右括号，还是仅插入左括号，还是什么都不做
            val editor = c.editor
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

    fun addColon(): InsertHandler<LookupElement> {
        return InsertHandler { c, _ ->
            // 按照当前的字符来决定是要插入冒号，还是什么都不做
            val editor = c.editor
            val caretOffset = editor.caretModel.offset
            val charsSequence = editor.document.charsSequence
            val c = charsSequence.get(caretOffset)
            when (c) {
                ':' -> {}
                else -> EditorModificationUtil.insertStringAtCaret(editor, ":", false, true, 1)
            }
        }
    }

    fun block(): InsertHandler<LookupElement> {
        return InsertHandler { c, _ ->
            // 插入成对的花括号
            applyBlock(c)
        }
    }

    fun keyOrValue(params: Params): InsertHandler<LookupElement> {
        return InsertHandler { c, _ -> applyKeyOrValue(c, params) }
    }

    fun keyWithValue(params: Params): InsertHandler<LookupElement> {
        return InsertHandler { c, _ -> applyKeyWithValue(c, params) }
    }

    fun applyBlock(c: InsertionContext) {
        val spaceWithinBraces = PlsCodeStyleUtil.isSpaceWithinBraces(c.file)
        val text = if (spaceWithinBraces) "{  }" else "{}"
        val length = if (spaceWithinBraces) text.length - 2 else text.length - 1
        EditorModificationUtil.insertStringAtCaret(c.editor, text, false, true, length)
    }

    fun applyKeyOrValue(context: InsertionContext, params: Params) {
        // `isKey` 如果是 `null`，则表示已经填充的只是键或值的其中一部分
        if (!params.quoted) return
        val editor = context.editor
        val caretOffset = editor.caretModel.offset
        val charsSequence = editor.document.charsSequence
        val rightQuoted = charsSequence.get(caretOffset) == '"' && charsSequence.get(caretOffset - 1) != '\\'
        if (rightQuoted) {
            // 在必要时将光标移到右双引号之后
            if (params.isKey != null) editor.caretModel.moveToOffset(caretOffset + 1)
        } else {
            // 插入缺失的右双引号，且在必要时将光标移到右双引号之后
            EditorModificationUtil.insertStringAtCaret(editor, "\"", false, params.isKey != null)
        }
    }

    fun applyKeyWithValue(context: InsertionContext, params: Params) {
        val editor = context.editor
        applyKeyOrValue(context, params)
        val spaceAroundPropertySeparator = PlsCodeStyleUtil.isSpaceAroundPropertySeparator(context.file)
        val spaceWithinBraces = PlsCodeStyleUtil.isSpaceWithinBraces(context.file)
        val text = buildString {
            if (spaceAroundPropertySeparator) append(" ")
            append("=")
            if (spaceAroundPropertySeparator) append(" ")
            if (params.insertCurlyBraces) {
                if (spaceWithinBraces) append("{  }") else append("{}")
            }
        }
        val length = if (params.insertCurlyBraces) {
            if (spaceWithinBraces) text.length - 2 else text.length - 1
        } else {
            text.length
        }
        EditorModificationUtil.insertStringAtCaret(editor, text, false, true, length)
    }

    data class Params(
        var quoted: Boolean = false,
        var isKey: Boolean? = null,
        var insertCurlyBraces: Boolean = false,
        var constantValue: String? = null,
    )
}
