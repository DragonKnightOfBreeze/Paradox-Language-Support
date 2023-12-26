package icu.windea.pls.script.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.util.*
import kotlin.math.*

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
        return this.setValue(text)
    }
    
    override fun createLiteralTextEscaper(): LiteralTextEscaper<ParadoxScriptStringExpressionElement> {
        return object : LiteralTextEscaper<ParadoxScriptStringExpressionElement>(this) {
            private var outSourceOffsets: IntArray? = null
            
            override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
                val subText = rangeInsideHost.substring(myHost.text)
                outSourceOffsets = IntArray(subText.length + 1)
                return ParadoxEscapeManager.parseScriptExpressionCharacters(subText, outChars, outSourceOffsets)
            }
            
            override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
                val outSourceOffsets = outSourceOffsets!!
                val result = if(offsetInDecoded < outSourceOffsets.size) outSourceOffsets[offsetInDecoded] else -1
                if(result == -1) return -1
                return min(result, rangeInsideHost.length) + rangeInsideHost.startOffset
            }
            
            override fun isOneLine(): Boolean {
                return myHost is ParadoxScriptPropertyKey
            }
        }
    }
}
