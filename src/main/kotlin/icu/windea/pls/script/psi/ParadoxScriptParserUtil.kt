@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun processTemplate(b: PsiBuilder, l: Int): Boolean {
        //interrupt parsing when contains whitespaces or comments
        val tokenType = b.rawLookup(-1)
        if(tokenType == TokenType.WHITE_SPACE) return false
        if(tokenType == COMMENT) return false
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
    
    @JvmStatic
    fun processSnippet(b: PsiBuilder, l: Int): Boolean {
        //remapping token types for parameter default values and inline parameter condition snippets
        if(b !is Builder) return true
        val containerType = b.state.currentFrame.elementType
        val templateType = b.state.currentFrame.parentFrame.elementType
        if(templateType !in ParadoxScriptTokenSets.TEMPLATE_TYPES && templateType != PROPERTY) return true
        b.setTokenTypeRemapper m@{ t, _, _, _ ->
            if(t in ParadoxScriptTokenSets.SNIPPET_TYPES) return@m SNIPPET_TOKEN
            if(containerType == PARAMETER) {
                if(t == INT_TOKEN || t == FLOAT_TOKEN) return@m SNIPPET_TOKEN
            }
            t
        }
        return true
    }
    
    @JvmStatic
    fun postProcessSnippet(b: PsiBuilder, l: Int) : Boolean {
        if(b !is Builder) return true
        val templateType = b.state.currentFrame.parentFrame.elementType
        if(templateType !in ParadoxScriptTokenSets.TEMPLATE_TYPES && templateType != PROPERTY) return true
        b.setTokenTypeRemapper(null)
        return true
    }
}
