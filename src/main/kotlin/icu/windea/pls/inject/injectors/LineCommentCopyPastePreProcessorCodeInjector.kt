@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.inject.injectors

import com.intellij.application.options.*
import com.intellij.lang.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import com.intellij.util.*
import com.intellij.util.text.*
import icu.windea.pls.cwt.*
import icu.windea.pls.cwt.codeStyle.*
import icu.windea.pls.cwt.editor.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

/**
 * @see com.intellij.codeInsight.editorActions.LineCommentCopyPastePreProcessor
 * @see com.intellij.codeInsight.editorActions.LineCommentCopyPastePreProcessor.preprocessOnPaste
 */
@InjectTarget("com.intellij.codeInsight.editorActions.LineCommentCopyPastePreProcessor")
class LineCommentCopyPastePreProcessorCodeInjector: CodeInjectorBase() {
    //用于兼容CWT语言的各种注释（行注释"# ..."，选项注释"## ..."，文档注释"### ..."）
    
    @InjectMethod
    fun preprocessOnPaste(project: Project, file: PsiFile, editor: Editor, text: String, rawText: RawText?): String {
        val language = file.language
        val commenter = LanguageCommenters.INSTANCE.forLanguage(language) ?: return text
        val lineCommentPrefix = commenter.lineCommentPrefix ?: return text
        
        val document = editor.document
        val offset = editor.selectionModel.selectionStart
        if(DocumentUtil.isAtLineEnd(offset, editor.document) && text.startsWith("\n")) return text

        val lineStartOffset = DocumentUtil.getLineStartOffset(offset, document)
        val chars = document.immutableCharSequence
        val firstNonWsLineOffset = CharArrayUtil.shiftForward(chars, lineStartOffset, " \t")
        
        fun matchesCommentPrefix(commentPrefix: String): Boolean {
            return offset >= (firstNonWsLineOffset + commentPrefix.length) 
                && CharArrayUtil.regionMatches(chars, firstNonWsLineOffset, commentPrefix)
        }
        
        var commentPrefix: String? = null
        var addSpace = false
        
        run {
            if(language == CwtLanguage) {
                if(matchesCommentPrefix(CwtCommenter.DOCUMENTATION_COMMENT_PREFIX)) {
                    commentPrefix = CwtCommenter.DOCUMENTATION_COMMENT_PREFIX
                    addSpace = CodeStyle.getSettings(file).getCustomSettings(CwtCodeStyleSettings::class.java).DOCUMENTATION_COMMENT_ADD_SPACE
                    return@run
                }
                if(matchesCommentPrefix(CwtCommenter.OPTION_COMMENT_PREFIX)) {
                    commentPrefix = CwtCommenter.OPTION_COMMENT_PREFIX
                    addSpace = CodeStyle.getSettings(file).getCustomSettings(CwtCodeStyleSettings::class.java).OPTION_COMMENT_ADD_SPACE
                    return@run
                }
            }
            if(matchesCommentPrefix(lineCommentPrefix)) {
                commentPrefix = lineCommentPrefix
                addSpace = CodeStyle.getSettings(file).getCommonSettings(language).LINE_COMMENT_ADD_SPACE
            }
        }
        val commentPrefix0 = commentPrefix ?: return text
        
        val s1 = chars.subSequence(lineStartOffset, firstNonWsLineOffset + commentPrefix0.length)
        val s2 = if(addSpace) " " else ""
        val lineStartReplacement = "\n$s1$s2"
        return StringUtil.trimTrailing(text, '\n').replace("\n", lineStartReplacement)
    }
}
