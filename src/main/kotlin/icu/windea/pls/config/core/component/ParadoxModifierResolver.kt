package icu.windea.pls.config.core.component

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

/**
 * 处理如何解析生成的修饰符，以及获取修饰符的生成源信息。
 * 
 * @see ParadoxModifierElement
 */
interface ParadoxModifierResolver {
    fun matchModifier(name: String, configGroup: CwtConfigGroup, matchType: Int) : Boolean
    
    fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement?
    
    //TODO 获取icon和modifierCategory
    
    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此解析器是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean
    
    companion object INSTANCE {
        @JvmStatic
        val EP_NAME = ExtensionPointName.create<ParadoxModifierResolver>("icu.windea.pls.paradoxModifierResolver")
    
        fun matchModifier(name: String, configGroup: CwtConfigGroup, matchType: Int) : Boolean {
            return EP_NAME.extensions.any { it.matchModifier(name, configGroup, matchType) }
        }
        
        fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.resolveModifier(name, element, configGroup) }
        }
        
        fun getDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean {
            return EP_NAME.extensions.any { it.buildDocumentationDefinition(element, builder) }
        }
    }
}

