@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.script.psi

import com.intellij.lang.*
import com.intellij.lang.parser.*
import com.intellij.psi.*

object ParadoxScriptParserUtil : GeneratedParserUtilBase() {
    @JvmStatic
    fun doParameter(b: PsiBuilder, l: Int): Boolean {
        //currentStep: PARAMETER_START
        if(b !is Builder) return true
        val parameterAwareFrame = b.state.currentFrame.parentFrame
        val elementType = parameterAwareFrame.elementType
        //包含参数的封装变量名、键、字符串、封装变量引用不能包含空白
        if(elementType in ParadoxScriptTokenSets.TEMPLATE_AWARE_TYPES) {
            if(b.rawTokenTypeStart(-1) != parameterAwareFrame.offset && b.rawLookup(-2) == TokenType.WHITE_SPACE) {
                return false
            }
        }
        //后面有属性分隔符的场合，不能解析为string或scriptedVariableName
        if(elementType in ParadoxScriptTokenSets.TEMPLATE_AWARE_RIGHT_TYPES) {
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
        }
        return true
    }
    
    @JvmStatic
    fun doInlineMathParameter(b: PsiBuilder, l: Int): Boolean {
        //与普通参数保持一致，目前不需要做进一步的处理
        return true
    }
}