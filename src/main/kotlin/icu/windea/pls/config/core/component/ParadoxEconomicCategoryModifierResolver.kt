package icu.windea.pls.config.core.component

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * 通过经济类型（`economic_category`）生成修饰符。
 */
@WithGameType(ParadoxGameType.Stellaris)
class ParadoxEconomicCategoryModifierResolver: ParadoxModifierResolver {
    // will be generated based on detailed declaration
    // <key>_<resource>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
    // 
    // will be generated if use_for_ai_budget = yes
    // <key>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
    
    companion object {
        @JvmField val economicCategoryInfoKey = Key.create<ParadoxEconomicCategoryInfo>("paradox.modifierElement.economicCategoryInfo")
        @JvmField val economicCategoryModifierInfoKey = Key.create<ParadoxEconomicCategoryModifierInfo>("paradox.modifierElement.economicCategoryModifierInfo")
        
        const val economicCategoriesDirPath = "common/economic_categories"
        const val economicCategoriesPathExpression = "common/economic_categories/,.txt"
    }
    
    fun isGameTypeSupported(gameType: ParadoxGameType): Boolean {
        return gameType == ParadoxGameType.Stellaris
    }
    
    override fun matchModifier(name: String, configGroup: CwtConfigGroup, matchType: Int): Boolean {
        val gameType = configGroup.gameType ?: return false
        if(!isGameTypeSupported(gameType)) return false
        val selector = definitionSelector().gameType(gameType) 
        var r = false
        ParadoxDefinitionSearch.search("economic_category", configGroup.project, selector = selector)
            .processQuery p@{
                val info = ParadoxEconomicCategoryHandler.getInfo(it) ?: return@p true
                for(modifierInfo in info.modifiers) {
                    if(modifierInfo.name == name) {
                        r = true
                        return@p false
                    }
                }
                true
            }
        return r
    }
    
    override fun resolveModifier(name: String, element: ParadoxScriptStringExpressionElement, configGroup: CwtConfigGroup): ParadoxModifierElement? {
        val gameType = configGroup.gameType ?: return null
        if(!isGameTypeSupported(gameType)) return null
        val project = configGroup.project
        var economicCategoryInfo: ParadoxEconomicCategoryInfo? = null
        var economicCategoryModifierInfo: ParadoxEconomicCategoryModifierInfo? = null
        val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
        ParadoxDefinitionSearch.search("economic_category", configGroup.project, selector = selector)
            .processQuery p@{
                val info = ParadoxEconomicCategoryHandler.getInfo(it) ?: return@p true
                for(modifierInfo in info.modifiers) {
                    if(modifierInfo.name == name) {
                        economicCategoryInfo = info
                        economicCategoryModifierInfo = modifierInfo
                        return@p false
                    }
                }
                true
            }
        if(economicCategoryInfo == null || economicCategoryModifierInfo == null) return null
        val result = ParadoxModifierElement(element, name, null, gameType, project)
        result.putUserData(economicCategoryInfoKey, economicCategoryInfo)
        result.putUserData(economicCategoryModifierInfoKey, economicCategoryModifierInfo)
        return result
    }
    
    override fun completeModifier(context: ProcessingContext, result: CompletionResultSet, modifierNames: MutableSet<String>): Unit = with(context) {
        val element = contextElement
        if(element !is ParadoxScriptStringExpressionElement) return
        val configGroup = configGroup
        val gameType = configGroup.gameType ?: return
        if(!isGameTypeSupported(gameType)) return
        val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
        ParadoxDefinitionSearch.search("economic_category", configGroup.project, selector = selector)
            .processQuery p@{
                val info = ParadoxEconomicCategoryHandler.getInfo(it) ?: return@p true
                
                //TODO
                //排除不匹配modifier的supported_scopes的情况
                //val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, modifierConfig.supportedScopes, configGroup)
                //if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
                
                val tailText = PlsDocBundle.message("fromEconomicCategory") + " " + info.name
                val typeText = info.name
                val typeIcon = PlsIcons.Definition
                for(modifierInfo in info.modifiers) {
                    val name = modifierInfo.name
                    //排除重复的
                    if(!modifierNames.add(name)) continue
                    
                    val modifierElement = resolveModifier(name, element, configGroup)
                    val builder = ParadoxScriptExpressionLookupElementBuilder.create(modifierElement, name)
                        .withIcon(PlsIcons.Modifier)
                        .withTailText(tailText)
                        .withTypeText(typeText)
                        .withTypeIcon(typeIcon)
                        .withScopeMatched(scopeMatched)
                    //.withPriority(PlsCompletionPriorities.modifierPriority)
                    result.addScriptExpressionElement(context, builder)
                }
                true
            }
    }
    
    override fun buildDocumentationDefinition(element: ParadoxModifierElement, builder: StringBuilder): Boolean = with(builder) {
        val gameType = element.gameType
        if(!isGameTypeSupported(gameType)) return@with false
        
        val economicCategoryInfo = element.getUserData(economicCategoryInfoKey) ?: return false
        val economicCategoryModifierInfo = element.getUserData(economicCategoryModifierInfoKey) ?: return false
        
        //加上名字
        val name = element.name
        append(PlsDocBundle.message("prefix.modifier")).append(" <b>").append(name.escapeXml().orAnonymous()).append("</b>")
        //加上经济类型信息
        appendBr().appendIndent()
        append(PlsDocBundle.message("generatedFromEconomicCategory"))
        append(" ")
        val economicCategoryName = economicCategoryInfo.name
        appendDefinitionLink(gameType, economicCategoryName, "economic_category", element)
        append(": ")
        val typeLink = "${gameType.id}/types/economic_category"
        appendCwtLink("economic_category", typeLink)
        val note = getModifierInfoNote(economicCategoryModifierInfo)
        if(note != null) appendBr().appendIndent().append(note)
        
        return true
    }
    
    private fun getModifierInfoNote(info: ParadoxEconomicCategoryModifierInfo): String? {
        return when {
            info.triggered && info.aiBudget -> PlsDocBundle.message("triggeredAndForAiBudget")
            info.triggered -> PlsDocBundle.message("triggered")
            info.aiBudget -> PlsDocBundle.message("forAiBudget")
            else -> null
        }
    }
    
    override fun buildDDocumentationDefinitionForDefinition(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, builder: StringBuilder): Boolean = with(builder) {
        val gameType = definitionInfo.gameType
        if(!isGameTypeSupported(gameType)) return false
        
        val configGroup = definitionInfo.configGroup
        val selector = definitionSelector().gameType(gameType).preferRootFrom(definition)
        val economicCategory = ParadoxDefinitionSearch.search(definitionInfo.name, "economic_category", configGroup.project, selector = selector)
            .findFirst()
            ?: return false
        val economicCategoryInfo = ParadoxEconomicCategoryHandler.getInfo(economicCategory) ?: return false
        val modifiers = economicCategoryInfo.modifiers
        if(modifiers.isEmpty()) return true //don't generate modifiers, return true
        for(modifier in modifiers) {
            appendBr()
            append(PlsDocBundle.message("prefix.generatedModifier")).append(" ")
            append(modifier.name)
            grayed {
                append(" ")
                append(PlsDocBundle.message("fromEconomicCategory"))
                append(" ")
                val economicCategoryName = economicCategoryInfo.name
                appendDefinitionLink(gameType, economicCategoryName, "economic_category", definition)
                append(": ")
                val typeLink = "${gameType.id}/types/economic_category"
                appendCwtLink("economic_category", typeLink)
            }
        }
        return true
    }
}