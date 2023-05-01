package icu.windea.pls.lang.inline

import com.intellij.openapi.extensions.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.util.*

/**
 * 提供对脚本成员元素的内联逻辑的支持。
 * 
 * 处理需要内联的脚本内容或者处理脚本片段时，将指定的成员元素内联为另一个成员元素，
 * 以便从另一个元素向上查找定义成员和定义，或者上下查找定义成员，或者获取需要的[ParadoxElementPath]。
 *
 * 注意内联前后的那些成员元素是不会包括在查找结果以及元素路径之内的。
 * 
 * 注意嵌套内联目前是允许的，但需要特别处理递归嵌套内联的情况。
 *
 * @see ParadoxScriptMemberElement
 */
interface ParadoxScriptMemberElementInlineSupport {
    fun canLink(element: ParadoxScriptMemberElement): Boolean
    
    fun linkElement(element: ParadoxScriptMemberElement, inlineStack: Deque<String>): ParadoxScriptMemberElement?
    
    fun inlineElement(element: ParadoxScriptMemberElement, inlineStack: Deque<String>): ParadoxScriptMemberElement?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxScriptMemberElementInlineSupport>("icu.windea.pls.scriptMemberElementInlineSupport")
        
        fun canLink(element: ParadoxScriptMemberElement): Boolean {
            return EP_NAME.extensionList.any { it.canLink(element) }
        }
        
        fun linkElement(element: ParadoxScriptMemberElement, inlineStack: Deque<String>): ParadoxScriptMemberElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.linkElement(element, inlineStack) }
        }
        
        fun inlineElement(element: ParadoxScriptMemberElement, inlineStack: Deque<String>): ParadoxScriptMemberElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.inlineElement(element, inlineStack) }
        }
    }
}

