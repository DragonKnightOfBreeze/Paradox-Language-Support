package icu.windea.pls.cwt.codeInsight.editorActions

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate.*
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import com.intellij.psi.util.startOffset
import icu.windea.pls.cwt.psi.CwtDocComment
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtFile

class CwtEnterHandler : EnterHandlerDelegate {
    override fun preprocessEnter(
        file: PsiFile,
        editor: Editor,
        caretOffset: Ref<Int>,
        caretAdvance: Ref<Int>,
        dataContext: DataContext,
        originalHandler: EditorActionHandler?
    ): Result {
        if (file !is CwtFile || !file.isValid) return Result.Continue
        val element = file.findElementAt(caretOffset.get()) ?: return Result.Continue

        handleDocComment(editor, element, caretOffset, caretAdvance)?.let { return it }

        return Result.Continue
    }

    private fun handleDocComment(editor: Editor, element: PsiElement, caretOffset: Ref<Int>, caretAdvance: Ref<Int>): Result? {
        // 当前光标位于文档注释所在行，且输入回车时，在必要时自动插入合适的文档注释前缀

        // EOL whitespace is not useful, we only need the tokens behind it
        var element = element

        if (element is PsiWhiteSpace) {
            // In multiline whitespaces, check cursor position to check whether the handler should trigger
            val whitespaces = element.text
            val end = caretOffset.get() - element.textOffset
            if (StringUtil.countChars(whitespaces, '\n', 0, end, false) > 0) {
                return Result.Continue
            }
            element = element.prevSibling
        }

        if (element.elementType == CwtElementTypes.DOC_COMMENT_TOKEN) {
            element = element.parent
        }

        // Check whether the desired caret element is doc comment
        if (element !is CwtDocComment) return null

        val startOffset = element.startOffset

        // Check whether the caret is after doc prefix
        if (caretOffset.get() - startOffset < 3) return null

        val document = editor.document
        val charsSequence = document.charsSequence
        val caretIsSpace = charsSequence.get(caretOffset.get()) == ' '
        val leadingSpaces = if (caretIsSpace) "" else " "
        val docPrefix = charsSequence.subSequence(startOffset, caretOffset.get())
            .takeWhile { it == '#' }.toString() + leadingSpaces

        document.insertString(caretOffset.get(), docPrefix)
        caretAdvance.set(docPrefix.length)

        return Result.DefaultForceIndent
    }
}
