package icu.windea.pls.lang.modifier

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*
import icu.windea.pls.model.data.*
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
    fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean
    
    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierData?
    
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>)
    
    fun getModificationTracker(modifierData: ParadoxModifierData): ModificationTracker? = null
    
    fun getModifierCategories(modifierElement: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>?
    
    /**
     * 构建修正的快速文档中的定义部分。
     * @return 此解析器是否适用。
     */
    fun buildDocumentationDefinition(modifierElement: ParadoxModifierElement, builder: StringBuilder): Boolean = false
    
    /**
     * 构建定义的快速文档中的定义部分中的对应的生成的修正的那一部分。
     * @return 此解析器是否适用。
     */
    fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: StringBuilder): Boolean = false
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxModifierSupport>("icu.windea.pls.modifierSupport")
        
        fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
            val gameType = configGroup.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f false
                ep.matchModifier(name, element, configGroup)
            }
        }
        
        fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierData? {
            val gameType = configGroup.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.resolveModifier(name, element, configGroup) ?: return@f null
            }
        }
        
        fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
            val gameType = selectGameType(context.originalFile)
            EP_NAME.extensionList.forEachFast f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f
                ep.completeModifier(context, result, modifierNames)
            }
        }
        
        fun getModifierCategories(element: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>? {
            val gameType = element.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getModifierCategories(element)
            }
        }
        
        fun getDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean {
            val gameType = element.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f false
                ep.buildDocumentationDefinition(element, builder)
            }
        }
        
        fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: StringBuilder): Boolean {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f false
                ep.buildDDocumentationDefinitionForDefinition(definition, definitionInfo, builder)
            }
        }
    }
    
    object Keys: KeysAware
}

val ParadoxModifierSupport.Keys.support by createKey<ParadoxModifierSupport>("paradox.modifier.support.support")
val ParadoxModifierSupport.Keys.modifierConfig by createKey<CwtModifierConfig>("paradox.modifier.support.modifierConfig")

var ParadoxModifierData.support by ParadoxModifierSupport.Keys.support
var ParadoxModifierData.modifierConfig by ParadoxModifierSupport.Keys.modifierConfig

var ParadoxModifierElement.support by ParadoxModifierSupport.Keys.support
var ParadoxModifierElement.modifierConfig by ParadoxModifierSupport.Keys.modifierConfig
