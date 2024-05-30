package icu.windea.pls.inject.injectors

import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.util.text.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.cwt.editor.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

/**
 * @see com.intellij.codeInsight.generation.CommentByLineCommentHandler
 * @see com.intellij.codeInsight.generation.CommentByLineCommentHandler.isLineCommented
 */
@InjectTarget("com.intellij.codeInsight.generation.CommentByLineCommentHandler")
class CommentByLineCommentHandlerCodeInjector: CodeInjectorBase() {
    //用于兼容CWT语言的各种注释（行注释"# ..."，选项注释"## ..."，文档注释"### ..."）
    
    private val Any.editor: Editor by memberProperty("editor", null) 
    
    @InjectMethod(pointer = InjectMethod.Pointer.BEFORE, static = true)
    fun isLineCommented(block: Any, line: Int, commenter: Commenter): Boolean {
        if(commenter is CwtCommenter) {
            val document = block.editor.document
            var lineStart: Int = document.getLineStartOffset(line)
            val chars: CharSequence = document.charsSequence
            lineStart = CharArrayUtil.shiftForward(chars, lineStart, " \t")
            val prefix = CwtCommenter.OPTION_COMMENT_PREFIX
            if(CharArrayUtil.regionMatches(chars, lineStart, prefix)) return false
        }
        continueInvocation()
    }
}
