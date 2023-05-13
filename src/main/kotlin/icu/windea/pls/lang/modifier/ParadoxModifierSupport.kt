package icu.windea.pls.lang.modifier

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 提供对修正的支持。
 *
 * @see ParadoxModifierElement
 */
@WithGameTypeEP
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
     * 构建定义的快速文档中的生成修正修正部分。
     */
    fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: StringBuilder): Boolean = false
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxModifierSupport>("icu.windea.pls.modifierSupport")
        
        fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup, matchType: Int): Boolean {
            val gameType = configGroup.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f false
                ep.matchModifier(name, element, configGroup, matchType)
            }
        }
        
        fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
            val gameType = configGroup.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.resolveModifier(name, element, configGroup)
                    ?.also { it.putUserData(ParadoxModifierHandler.supportKey, ep) }
            }
        }
        
        fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
            val gameType = selectGameType(context.originalFile)
            EP_NAME.extensionList.forEach f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                ep.completeModifier(context, result, modifierNames)
            }
        }
        
        fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
            val ep = element.getUserData(ParadoxModifierHandler.supportKey) ?: return null
            return ep.getModifierCategories(element)
            
            //val gameType = element.gameType
            //return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
            //    if(!gameType.supportsByAnnotation(ep)) return@f null
            //    ep.getModifierCategories(element)
            //}
        }
        
        fun getDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean {
            val ep = element.getUserData(ParadoxModifierHandler.supportKey) ?: return false
            return ep.buildDocumentationDefinition(element, builder)
            
            //val gameType = element.gameType
            //return EP_NAME.extensionList.any f@{ ep ->
            //    if(!gameType.supportsByAnnotation(ep)) return@f false
            //    ep.buildDocumentationDefinition(element, builder)
            //}
        }
        
        fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: StringBuilder): Boolean {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f false
                ep.buildDDocumentationDefinitionForDefinition(definition, definitionInfo, builder)
            }
        }
    }
}
