package icu.windea.pls.script.psi

import ai.grazie.text.*
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*

/**
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 */
interface ParadoxScriptStringExpressionElement : ParadoxScriptExpressionElement, ContributedReferenceHost, ParadoxLanguageInjectionHost {
    override fun getName(): String
    
    override val value: String
    
    override fun setValue(value: String): ParadoxScriptStringExpressionElement
    
    override fun isValidHost(): Boolean {
        return true
    }
    
    override fun updateText(text: String): ParadoxScriptStringExpressionElement {
        if(text.isLeftQuoted() && text.isRightQuoted()) {
            //should be unquoted first here
            val oldText = this.text
            val unquotedTextRange = TextRange.create(0, oldText.length).unquote(oldText)
            val unquotedText = text.substring(1, text.length - 1)
            return ElementManipulators.handleContentChange(this,  unquotedTextRange, unquotedText)
        }
        return ElementManipulators.handleContentChange(this, text)
    }
    
    override fun createLiteralTextEscaper(): LiteralTextEscaper<ParadoxScriptStringExpressionElement> {
        return ParadoxScriptExpressionLiteralTextEscaper(this)
    }
}
