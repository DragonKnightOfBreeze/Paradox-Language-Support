@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.*
import com.intellij.psi.tree.*

object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun doParameter(b: PsiBuilder, l: Int): Boolean {
        //currentStep: PARAMETER_START
        if(b !is Builder) return true
        val parameterAwareFrame = b.state.currentFrame.parentFrame
        val elementType = parameterAwareFrame.elementType
        //包含参数的封装变量名、键、字符串、封装变量引用不能包含空白
        if(elementType in ParadoxScriptTokenSets.SNIPPET_AWARE_TYPES) {
            if(b.rawTokenTypeStart(-1) != parameterAwareFrame.offset && b.rawLookup(-2) == TokenType.WHITE_SPACE) {
                return false
            }
        }
        //后面有属性分隔符的场合，不能解析为string或scriptedVariableName
        if(elementType in ParadoxScriptTokenSets.SNIPPET_AWARE_RIGHT_TYPES) {
            if(b.rawLookupSkipWhiteSpace(0) in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS) {
                return false
            }
        }
        return true
    }
    
    @JvmStatic
    fun doInlineMathParameter(b: PsiBuilder, l: Int): Boolean {
        return true
    }
    
    fun PsiBuilder.rawLookupSkipWhiteSpace(steps: Int): IElementType? {
        var c = steps
        while(true) {
            val r = rawLookup(c)
            if(r != TokenType.WHITE_SPACE) return r
            c++
        }
    }
}