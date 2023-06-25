package icu.windea.pls.lang.inline

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.script.psi.*

/**
 * 提供对脚本片段的内联逻辑的支持。
 *
 * 注意嵌套内联目前是允许的，但需要特别处理递归嵌套内联的情况。
 * 
 * 目前仅限内联脚本。
 */
interface ParadoxInlineSupport {
    fun inlineElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxInlineSupport>("icu.windea.pls.inlineSupport")
        
        fun inlineElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.inlineElement(element) 
            }
        }
    }
}

