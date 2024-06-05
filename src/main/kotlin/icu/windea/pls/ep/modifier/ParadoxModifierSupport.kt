package icu.windea.pls.ep.modifier

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.documentation.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.elementInfo.*
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
    
    fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo?
    
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>)
    
    fun getModificationTracker(modifierInfo: ParadoxModifierInfo): ModificationTracker? = null
    
    fun getModifierCategories(modifierElement: ParadoxModifierElement): Map<String, CwtModifierCategoryConfig>?
    
    /**
     * 构建修正的快速文档中的定义部分。
     * @return 此解析器是否适用。
     */
    fun buildDocumentationDefinition(modifierElement: ParadoxModifierElement, builder: DocumentationBuilder): Boolean = false
    
    /**
     * 构建定义的快速文档中的定义部分中的对应的生成的修正的那一部分。
     * @return 此解析器是否适用。
     */
    fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean = false
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxModifierSupport>("icu.windea.pls.modifierSupport")
        
        fun matchModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): Boolean {
            val gameType = configGroup.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f false
                ep.matchModifier(name, element, configGroup)
            }
        }
        
        fun resolveModifier(name: String, element: PsiElement, configGroup: CwtConfigGroup): ParadoxModifierInfo? {
            val gameType = configGroup.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.resolveModifier(name, element, configGroup)
                    ?.also { it.support = ep }
            }
        }
        
        fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>) {
            val gameType = context.gameType ?: return
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
        
        fun getDocumentationDefinition(element: ParadoxModifierElement, builder: DocumentationBuilder): Boolean {
            val gameType = element.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f false
                ep.buildDocumentationDefinition(element, builder)
            }
        }
        
        fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: DocumentationBuilder): Boolean {
            val gameType = definitionInfo.gameType
            return EP_NAME.extensionList.any f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f false
                ep.buildDDocumentationDefinitionForDefinition(definition, definitionInfo, builder)
            }
        }
    }
    
    object Keys: KeyRegistry()
}

val ParadoxModifierSupport.Keys.support by createKey<ParadoxModifierSupport>("paradox.modifier.support.support")
val ParadoxModifierSupport.Keys.modifierConfig by createKey<CwtModifierConfig>("paradox.modifier.support.modifierConfig")

var ParadoxModifierInfo.support by ParadoxModifierSupport.Keys.support
var ParadoxModifierInfo.modifierConfig by ParadoxModifierSupport.Keys.modifierConfig

var ParadoxModifierElement.support by ParadoxModifierSupport.Keys.support
var ParadoxModifierElement.modifierConfig by ParadoxModifierSupport.Keys.modifierConfig
