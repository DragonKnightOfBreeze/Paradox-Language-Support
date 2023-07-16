package icu.windea.pls.core.psi

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.model.*

interface ParadoxParameter : ParadoxTypedElement, NavigatablePsiElement, ParadoxLanguageInjectionHost {
    override fun getName(): String?
    
    fun setName(name: String): ParadoxParameter
    
    val defaultValue: String? get() = null
    
    override val type: ParadoxType get() = ParadoxType.Parameter
    
    override fun isValidHost(): Boolean {
        return true
    }
    
    override fun updateText(text: String): ParadoxParameter {
        return this.setName(text)
    }
    
    override fun createLiteralTextEscaper(): LiteralTextEscaper<ParadoxParameter> {
        return object : LiteralTextEscaper<ParadoxParameter>(this) {
            override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
                outChars.append(rangeInsideHost.substring(myHost.text))
                return true
            }
            
            override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
                return rangeInsideHost.startOffset + offsetInDecoded
            }
            
            override fun isOneLine(): Boolean {
                return true
            }
        }
    }
}

