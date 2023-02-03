package icu.windea.pls.config.core.component

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.extensions.*
import com.intellij.util.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

/**
 * 处理如何解析生成的修饰符，以及获取修饰符的生成源信息。
 *
 * @see ParadoxModifierElement
 */
interface ParadoxModifierResolver {
    fun matchModifier(name: String, configGroup: CwtConfigGroup, matchType: Int): Boolean
    
    fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement?
    
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>)
    
    //TODO 获取icon和modifierCategory
    
    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此解析器是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean = false
    
    /**
     * 构建定义的快速文档中的生成修饰符修正部分。
     */
    fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: StringBuilder) : Boolean = false
    
    companion object INSTANCE {
        @JvmStatic
        val EP_NAME = ExtensionPointName.create<ParadoxModifierResolver>("icu.windea.pls.paradoxModifierResolver")
        
        fun matchModifier(name: String, configGroup: CwtConfigGroup, matchType: Int): Boolean {
            return EP_NAME.extensions.any { it.matchModifier(name, configGroup, matchType) }
        }
        
        fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.resolveModifier(name, element, configGroup) }
        }
        
        fun getDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean {
            return EP_NAME.extensions.any { it.buildDocumentationDefinition(element, builder) }
        }
        
        fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: StringBuilder): Boolean{
            return EP_NAME.extensions.any { it.buildDDocumentationDefinitionForDefinition(definition, definitionInfo, builder) }
        }
        
        fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
            EP_NAME.extensions.forEach { it.completeModifier(context, result, modifierNames) }
        }
    }
}
