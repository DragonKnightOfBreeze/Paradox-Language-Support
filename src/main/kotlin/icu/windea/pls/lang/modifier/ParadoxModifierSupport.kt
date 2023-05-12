package icu.windea.pls.lang.modifier

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 提供对生成的修饰符的支持。
 *
 * 例如，如何解析生成的修饰符，如何获取修饰符的生成源信息。
 *
 * @see ParadoxModifierElement
 */
interface ParadoxModifierSupport {
    /**
     * @param element 进行匹配时的上下文PSI元素。
     */
    fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, matchType: Int): Boolean
    
    fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement?
    
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>)
    
    fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>?
    
    //TODO 获取icon
    
    /**
     * 构建参数的快速文档中的定义部分。
     * @return 此解析器是否适用。
     */
    fun buildDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean = false
    
    /**
     * 构建定义的快速文档中的生成修饰符修正部分。
     */
    fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: StringBuilder): Boolean = false
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxModifierSupport>("icu.windea.pls.modifierSupport")
        
        fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, matchType: Int): Boolean {
            return EP_NAME.extensionList.any { it.matchModifier(name, element, configGroup, matchType) }
        }
        
        fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.resolveModifier(name, element, configGroup) }
        }
        
        fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
            EP_NAME.extensionList.forEach { it.completeModifier(context, result, modifierNames) }
        }
        
        fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.getModifierCategories(element) }
        }
        
        fun getDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean {
            return EP_NAME.extensionList.any { it.buildDocumentationDefinition(element, builder) }
        }
        
        fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: StringBuilder): Boolean {
            return EP_NAME.extensionList.any { it.buildDDocumentationDefinitionForDefinition(definition, definitionInfo, builder) }
        }
    }
}
