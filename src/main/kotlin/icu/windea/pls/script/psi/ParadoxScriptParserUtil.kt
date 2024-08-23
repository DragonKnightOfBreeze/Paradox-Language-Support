@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun checkRightTemplate(b: PsiBuilder, l: Int): Boolean {
        //a token should not be parsed to a value when with a trailing separator
        var s = -1
        var end = false
        while(true) {
            s++
            val t = b.rawLookup(s)
            when{
                t == null -> break
                t == TokenType.WHITE_SPACE -> end = true
                t in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS -> return false
                else -> if(end) break
            }
        }
        return true
    }
    
    @JvmStatic
    fun processTemplate(b: PsiBuilder, l: Int): Boolean {
        //interrupt parsing when contains whitespaces or comments
        val tokenType = b.rawLookup(-1)
        if(tokenType == TokenType.WHITE_SPACE) return false
        if(tokenType == COMMENT) return false
        //also for continuous literals
        if(tokenType in ParadoxScriptTokenSets.SNIPPET_TYPES) {
            val nextTokenType = b.rawLookup(0)
            if(nextTokenType != null && nextTokenType in ParadoxScriptTokenSets.SNIPPET_TYPES) return false
        }
        return true
    }
    
    @JvmStatic
    fun processInlineParameterCondition(b: PsiBuilder, l: Int): Boolean {
        //interrupt parsing when contains whitespaces or comments
        var i1 = -2
        run {
            val t1 = b.rawLookup(i1) ?: return@run
            if(t1 in ParadoxScriptTokenSets.SNIPPET_TYPES) return true
            if(t1 == PARAMETER_END) return true
            if(t1 == TokenType.WHITE_SPACE) i1--
            if(b.rawLookup(i1) in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) return true
        }
        var i2 = 1
        var n = 1
        while(true) {
            val t2 = b.rawLookup(i2) ?: break
            if(n == 0) {
                if(t2 in ParadoxScriptTokenSets.SNIPPET_TYPES) return true
                if(t2 == PARAMETER_START) return true
                if(t2 == TokenType.WHITE_SPACE) i2++
                if(b.rawLookup(i2) in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) return true
                break
            } else {
                if(t2 == TokenType.WHITE_SPACE) return false
                if(t2 == COMMENT) return false
            }
            if(t2 == LEFT_BRACKET) n++
            if(t2 == RIGHT_BRACKET) n--
            i2++
        }
        return false
    }
}
