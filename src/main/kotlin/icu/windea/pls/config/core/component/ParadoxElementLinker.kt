package icu.windea.pls.config.core.component

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.script.psi.*

/**
 * 处理需要内联的脚本内容或者处理脚本片段时，将指定的元素连接到另一个元素，
 * 以便从另一个元素向上查找定义成员和定义，或者上下查找定义成员，或者获取需要的[ParadoxElementPath]。
 */
interface ParadoxElementLinker {
    companion object INSTANCE {
        @JvmStatic val EP_NAME = ExtensionPointName.create<ParadoxElementLinker>("icu.windea.pls.paradoxElementLinker")
        
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
    
    fun canLink(element: ParadoxScriptMemberElement): Boolean
    
    fun linkElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement?
    
    fun inlineElement(element: ParadoxScriptMemberElement): ParadoxScriptMemberElement?
}
