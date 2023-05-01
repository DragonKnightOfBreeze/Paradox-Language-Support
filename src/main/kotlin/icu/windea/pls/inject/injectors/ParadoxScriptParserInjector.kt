@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.inject.injectors

import com.intellij.lang.*
import icu.windea.pls.inject.*
import icu.windea.pls.script.psi.*

/**
 * 用于完善脚本文件解析器的代码逻辑。
 */
@InjectTarget("icu.windea.pls.script.psi.ParadoxScriptParser", pluginId = "icu.windea.pls")
class ParadoxScriptParserInjector : BaseCodeInjector() {
    @Inject(Inject.Pointer.BEFORE)
    fun parameter(b: PsiBuilder, l: Int): Boolean {
        if(doParameter(b, l)) throw ContinueInvocationException.INSTANCE
        return false
    }
    
    private fun doParameter(b: PsiBuilder, l: Int): Boolean {
        //包含参数的封装变量名、键、字符串、封装变量引用不能包含空白
        val currentOffset = b.currentOffset
        if(currentOffset == 0) return true
        val tokenType = b.tokenType ?: return true
        if(tokenType !in ParadoxScriptTokenSets.SNIPPET_TOKENS) return true
        if(!Character.isWhitespace(b.originalText.get(currentOffset - 1))) return true
        return false
    }
}