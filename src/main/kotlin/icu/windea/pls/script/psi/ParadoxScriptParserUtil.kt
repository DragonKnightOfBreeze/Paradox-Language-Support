@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.*

object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun processTemplate(b: PsiBuilder, l: Int): Boolean {
        val tokenType = b.rawLookup(-1)
        if(tokenType == TokenType.WHITE_SPACE) return false
        if(tokenType == ParadoxScriptElementTypes.COMMENT) return false
        return true
    }
    
    @JvmStatic
    fun processInlineParameterCondition(b: PsiBuilder, l: Int): Boolean {
        var i1 = -2
        run {
            val t1 = b.rawLookup(i1) ?: return@run
            if(t1 == ParadoxScriptElementTypes.STRING_TOKEN) return true
            if(t1 == ParadoxScriptElementTypes.PARAMETER_END) return true
            if(t1 == TokenType.WHITE_SPACE) i1--
            if(b.rawLookup(i1) in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) return true
        }
        var i2 = 1
        var n = 1
        while(true) {
            val t2 = b.rawLookup(i2) ?: break
            if(n == 0) {
                if(t2 == ParadoxScriptElementTypes.STRING_TOKEN) return true
                if(t2 == ParadoxScriptElementTypes.PARAMETER_START) return true
                if(t2 == TokenType.WHITE_SPACE) i2++
                if(b.rawLookup(i2) in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) return true
                break
            } else {
                //inline parameter condition cannot contain any whitespace (or comment)
                if(t2 == TokenType.WHITE_SPACE) return false
                if(t2 == ParadoxScriptElementTypes.COMMENT) return false
            }
            if(t2 == ParadoxScriptElementTypes.LEFT_BRACKET) n++
            if(t2 == ParadoxScriptElementTypes.RIGHT_BRACKET) n--
            i2++
        }
        return false
    }
}
