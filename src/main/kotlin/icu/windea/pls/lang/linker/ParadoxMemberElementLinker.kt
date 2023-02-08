package icu.windea.pls.lang.linker

import com.intellij.openapi.extensions.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 处理需要内联的脚本内容或者处理脚本片段时，将指定的成员元素连接到另一个成员元素，
 * 以便从另一个元素向上查找定义成员和定义，或者上下查找定义成员，或者获取需要的[ParadoxElementPath]。
 * 
 * 注意连接前后的成员元素不会包括在查找结果内。
 *
 * @see ParadoxScriptMemberElement
 */
interface ParadoxMemberElementLinker {
    fun canLink(element: ParadoxScriptMemberElement): Boolean
    
    fun linkElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement?
    
    fun inlineElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement?
    
    companion object INSTANCE {
        @JvmStatic val EP_NAME = ExtensionPointName.create<ParadoxMemberElementLinker>("icu.windea.pls.paradoxMemberElementLinker")
        
        fun canLink(element: ParadoxScriptMemberElement): Boolean {
            return EP_NAME.extensions.any { it.canLink(element) }
        }
        
        fun linkElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.linkElement(element) }
        }
        
        fun inlineElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.inlineElement(element) }
        }
    }
}
