package icu.windea.pls.inject.injectors

import com.intellij.openapi.util.text.*
import com.intellij.util.text.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*

/**
 * 重写IDE低层检查字符串是否是标识符的代码逻辑，以便兼容本地化文件中的本地化图标引用（`£unity£`），从而可以正确地查找引用。
 */
@InjectTarget("com.intellij.psi.impl.search.LowLevelSearchUtil")
class LowLevelSearchUtilCodeInjector: BaseCodeInjector(){
    //com.intellij.psi.impl.search.LowLevelSearchUtil
    //com.intellij.psi.impl.search.LowLevelSearchUtil.checkJavaIdentifier
    
    //rewrite this method to compatible with Paradox localisation icon references (e.g. "£unity£")
    @Inject
    fun checkJavaIdentifier(text: CharSequence, searcher: StringSearcher, index: Int): Boolean {
        if(!searcher.isJavaIdentifier) {
            return true
        }
        if(index > 0) {
            val c = text[index - 1]
            if(c != '£') { //'£'
                if(Character.isJavaIdentifierPart(c) && c != '$') {
                    if(!searcher.isHandleEscapeSequences || index < 2 || StringUtil.isEscapedBackslash(text, 0, index - 2)) { //escape sequence
                        return false
                    }
                } else if(searcher.isHandleEscapeSequences && !StringUtil.isEscapedBackslash(text, 0, index - 1)) {
                    return false
                }
            }
        }
        val patternLength = searcher.pattern.length
        if(index + patternLength < text.length) {
            val c = text[index + patternLength]
            if(c != '£') { //'£'
                return !Character.isJavaIdentifierPart(c) || c == '$'
            }
        }
        return true
    }
}