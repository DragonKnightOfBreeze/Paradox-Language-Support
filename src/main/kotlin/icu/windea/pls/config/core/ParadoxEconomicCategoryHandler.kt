package icu.windea.pls.config.core

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*
import java.lang.invoke.*

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxEconomicCategoryHandler {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    val modifierCategoriesKey = Key.create<Set<String>>("paradox.economicCategory.modifierCategories")
    
    /**
     * 输入[definition]的定义类型应当保证是`economic_category`。
     */
    fun getInfo(definition: ParadoxScriptDefinitionElement): ParadoxEconomicCategoryInfo? {
        return getInfoFromCache(definition)
    }
    
    private fun getInfoFromCache(definition: ParadoxScriptDefinitionElement): ParadoxEconomicCategoryInfo? {
        if(definition !is ParadoxScriptProperty) return null
        return CachedValuesManager.getCachedValue(definition, PlsKeys.cachedEconomicCategoryInfoKey) {
            val value = resolveInfo(definition)
            CachedValueProvider.Result.create(value, definition)
        }
    }
    
    private fun resolveInfo(definition: ParadoxScriptProperty): ParadoxEconomicCategoryInfo? {
        //这种写法可能存在一定性能问题，但是问题不大
        //需要兼容继承的mult修饰符
        try {
            val data = ParadoxScriptDataResolver.resolveProperty(definition) ?: return null
            val name = definition.name.takeIfNotEmpty() ?: return null
            val parent = data.getValue("parent", valid = true)?.stringValue()
            val useForAiBudget = data.getValue("use_for_ai_budget")?.booleanValue() ?: false
            val modifiers = mutableSetOf<ParadoxEconomicCategoryModifierInfo>()
            val modifierCategory = data.getValue("modifier_category", valid = true)?.stringValue()
            
            val resources = getResources(definition)
                .takeIfNotEmpty() ?: return null //unexpected
            val generateAddModifiers = data.getValues("generate_add_modifiers/-", valid = true)
                .mapNotNull { it.stringValue() }
            val generateMultModifiers = data.getValues("generate_mult_modifiers/-", valid = true)
                .mapNotNull { it.stringValue() }
            val triggeredProducesModifiers = data.getValues("triggered_produces_modifier")
                .mapNotNull { resolveTriggeredModifier(it) }
            val triggeredCostModifiers = data.getValues("triggered_cost_modifier")
                .mapNotNull { resolveTriggeredModifier(it) }
            val triggeredUpkeepModifiers = data.getValues("triggered_upkeep_modifier")
                .mapNotNull { resolveTriggeredModifier(it) }
    
            // will generate if use_for_ai_budget = yes (inherited by parent property for _mult modifiers)
            // <economic_category>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
            // will generate:
            // <economic_category>_<resource>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
            
            fun addModifiers(resource: String?) {
                val target = if(resource != null) "_${resource}" else ""
                generateAddModifiers.forEach { category ->
                    modifiers.add(ParadoxEconomicCategoryModifierInfo("${name}${target}_${category}_add", resource, false))
                }
                generateMultModifiers.forEach { category ->
                    modifiers.add(ParadoxEconomicCategoryModifierInfo("${name}${target}_${category}_mult", resource, false))
                }
                triggeredProducesModifiers.forEach { (key, useParentIcon, types) ->
                    types.forEach { type ->
                        modifiers.add(ParadoxEconomicCategoryModifierInfo("${key}${target}_produces_${type}", resource, true, useParentIcon))
                    }
                }
                triggeredCostModifiers.forEach { (key, useParentIcon, types) ->
                    types.forEach { type ->
                        modifiers.add(ParadoxEconomicCategoryModifierInfo("${key}${target}_cost_${type}", resource, true, useParentIcon))
                    }
                }
                triggeredUpkeepModifiers.forEach { (key, useParentIcon, types) ->
                    types.forEach { type ->
                        modifiers.add(ParadoxEconomicCategoryModifierInfo("${key}${target}_upkeep_${type}", resource, true, useParentIcon))
                    }
                }
            }
    
            if(useForAiBudget) addModifiers(null)
            resources.forEach { resource -> addModifiers(resource) }
            
            return ParadoxEconomicCategoryInfo(name, parent, useForAiBudget, modifiers, modifierCategory)
        } catch(e: Exception) {
            logger.error(e)
            return null
        }
    }
    
    private fun getResources(contextElement: PsiElement): Set<String> {
        val selector = definitionSelector().gameTypeFrom(contextElement)
        return ParadoxDefinitionSearch.search("resource", contextElement.project, selector = selector)
            .mapNotNullTo(mutableSetOf()) { it.name }  //it.name is ok
    }
    
    private fun resolveTriggeredModifier(data: ParadoxScriptData): ParadoxTriggeredModifierInfo? {
        //key, modifier_types, use_parent_icon
        val key = data.getValue("key")?.stringValue() ?: return null
        val useParentIcon = data.getValue("use_parent_icon")?.booleanValue() ?: false
        val modifierTypes = data.getValues("modifier_types").mapNotNull { it.stringValue() }.takeIfNotEmpty() ?: return null
        return ParadoxTriggeredModifierInfo(key, useParentIcon, modifierTypes)
    }
    
    fun resolveModifierCategory(value: String?, configGroup: CwtConfigGroup): Map<String, CwtModifierCategoryConfig>{
        val finalValue = value ?: "economic_unit" //default to economic_unit
        val enumConfig = configGroup.enums.getValue("scripted_modifier_categories")
        var keys = getModifierCategoryKeys(enumConfig, finalValue)
        if(keys == null) keys = getModifierCategoryKeys(enumConfig, "economic_unit")
        if(keys == null) keys = emptySet() //unexpected
        val modifierCategories = configGroup.modifierCategories
        return keys.associateWith { modifierCategories.getValue(it) }
    }
    
    private fun getModifierCategoryKeys(enumConfig: CwtEnumConfig, finalValue: String): Set<String>? {
        val valueConfig = enumConfig.valueConfigMap.getValue(finalValue)
        return valueConfig.getOrPutUserData(modifierCategoriesKey) {
            valueConfig.options?.find { it.key == "modifier_categories" }
                ?.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue }?.takeIfNotEmpty()
        }
    }
}