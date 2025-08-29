@file:Suppress("unused")

package icu.windea.pls.inject.injectors

import com.intellij.lang.Commenter
import com.intellij.openapi.editor.Editor
import com.intellij.util.text.CharArrayUtil
import icu.windea.pls.core.memberProperty
import icu.windea.pls.cwt.editor.CwtCommenter
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectTarget

/**
 * @see com.intellij.codeInsight.generation.CommentByLineCommentHandler
 * @see com.intellij.codeInsight.generation.CommentByLineCommentHandler.isLineCommented
 */
@InjectTarget("com.intellij.codeInsight.generation.CommentByLineCommentHandler")
class CommentByLineCommentHandlerCodeInjector : CodeInjectorBase() {
    //用于兼容CWT语言的各种注释（行注释"# ..."，选项注释"## ..."，文档注释"### ..."）

    private val Any.editor: Editor by memberProperty("editor", null)

    @InjectMethod(pointer = InjectMethod.Pointer.BEFORE, static = true)
    fun isLineCommented(block: Any, line: Int, commenter: Commenter): Boolean {
        if (commenter is CwtCommenter) {
            val document = block.editor.document
            var lineStart: Int = document.getLineStartOffset(line)
            val chars: CharSequence = document.charsSequence
            lineStart = CharArrayUtil.shiftForward(chars, lineStart, " \t")
            val prefix = CwtCommenter.OPTION_COMMENT_PREFIX
            if (CharArrayUtil.regionMatches(chars, lineStart, prefix)) return false
        }
        continueInvocation()
    }
}

