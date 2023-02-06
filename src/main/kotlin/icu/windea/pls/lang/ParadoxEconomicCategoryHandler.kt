package icu.windea.pls.lang

import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.lang.model.*
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
    @JvmStatic
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
        //兼容继承的mult修饰符
        try {
            val data = ParadoxScriptDataResolver.resolveProperty(definition, inline = true) ?: return null
            val name = definition.name.takeIfNotEmpty() ?: return null
            val parent = data.getData("parent")?.value?.stringValue()
            val useForAiBudget = data.getData("use_for_ai_budget")?.value?.booleanValue()
                ?: getUseForAiBudgetFromParent(name, parent, definition)
            val modifiers = mutableSetOf<ParadoxEconomicCategoryModifierInfo>()
            val modifierCategory = data.getData("modifier_category")?.value?.stringValue()
            
            val resources = getResources(definition)
                .takeIfNotEmpty() ?: return null //unexpected
            val generateAddModifiers = data.getAllData("generate_add_modifiers/-")
                .mapNotNull { it.value?.stringValue() }
            val generateMultModifiers = data.getAllData("generate_mult_modifiers/-")
                .mapNotNull { it.value?.stringValue() }
            val triggeredProducesModifiers = data.getAllData("triggered_produces_modifier")
                .mapNotNull { resolveTriggeredModifier(it) }
            val triggeredCostModifiers = data.getAllData("triggered_cost_modifier")
                .mapNotNull { resolveTriggeredModifier(it) }
            val triggeredUpkeepModifiers = data.getAllData("triggered_upkeep_modifier")
                .mapNotNull { resolveTriggeredModifier(it) }
            
            // will generate if use_for_ai_budget = yes (inherited by parent property for _mult modifiers)
            // <economic_category>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
            // will generate:
            // <economic_category>_<resource>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
            
            fun addModifier(key: String, category: String, type: String, triggered: Boolean, useParentIcon: Boolean) {
                fun addModifier(modifierName: String, resource: String?) {
                    modifiers.add(ParadoxEconomicCategoryModifierInfo(modifierName, resource, triggered, useParentIcon))
                }
                
                if(useForAiBudget && triggered) {
                    addModifier("${key}_${category}_${type}", null)
                }
                resources.forEach { resource ->
                    addModifier("${key}_${resource}_${category}_${type}", resource)
                }
                if(useForAiBudget && !triggered) {
                    addModifier("${key}_${category}_${type}", null)
                }
            }
            
            generateAddModifiers.forEach { category ->
                val type = "add"
                val triggered = false
                val useParentIcon = false
                addModifier(name, category, type, triggered, useParentIcon)
            }
            generateMultModifiers.forEach { category ->
                val type = "mult"
                val triggered = false
                val useParentIcon = false
                addModifier(name, category, type, triggered, useParentIcon)
            }
            triggeredProducesModifiers.forEach { (key, useParentIcon, types) ->
                val category = "produces"
                val triggered = false
                types.forEach { type ->
                    addModifier(key, category, type, triggered, useParentIcon)
                }
            }
            triggeredCostModifiers.forEach { (key, useParentIcon, types) ->
                val category = "cost"
                val triggered = false
                types.forEach { type ->
                    addModifier(key, category, type, triggered, useParentIcon)
                }
            }
            triggeredUpkeepModifiers.forEach { (key, useParentIcon, types) ->
                val category = "upkeep"
                val triggered = false
                types.forEach { type ->
                    addModifier(key, category, type, triggered, useParentIcon)
                }
            }
            
            return ParadoxEconomicCategoryInfo(name, parent, useForAiBudget, modifiers, modifierCategory)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            logger.error(e)
            return null
        }
    }
    
    private fun getUseForAiBudgetFromParent(source: String, parent: String?, contextElement: ParadoxScriptProperty): Boolean {
        if(parent == null) return false // no parent > return false
        if(source == parent) return false //recursive parent > invalid, return false
        val file = contextElement.containingFile ?: return false
        val project = file.project
        val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
        return doGetUseForAiBudgetFromParent(source, parent, project, selector)
    }
    
    private fun doGetUseForAiBudgetFromParent(source: String, parent: String, project: Project, selector: ParadoxDefinitionSelector): Boolean {
        val parentElement = ParadoxDefinitionSearch.search(parent, "economic_category", project, selector = selector).find()
        val newParent = parentElement?.findProperty("parent", inline = true)?.propertyValue?.stringValue()
        if(source == newParent) return false //recursive parent > invalid, return false
        if(newParent != null) return doGetUseForAiBudgetFromParent(source, newParent, project, selector)
        val useForAiBudget = parentElement?.findProperty("use_for_ai_budget")?.propertyValue?.booleanValue()
        return useForAiBudget ?: false
    }
    
    private fun getResources(contextElement: PsiElement): Set<String> {
        val selector = definitionSelector().gameTypeFrom(contextElement)
        return ParadoxDefinitionSearch.search("resource", contextElement.project, selector = selector)
            .mapNotNullTo(mutableSetOf()) { it.name }  //it.name is ok
    }
    
    private fun resolveTriggeredModifier(data: ParadoxScriptData): ParadoxTriggeredModifierInfo? {
        //key, modifier_types, use_parent_icon
        val key = data.getData("key")?.value?.stringValue() ?: return null
        val useParentIcon = data.getData("use_parent_icon")?.value?.booleanValue() ?: false
        val modifierTypes = data.getAllData("modifier_types").mapNotNull { it.value?.stringValue() }.takeIfNotEmpty() ?: return null
        return ParadoxTriggeredModifierInfo(key, useParentIcon, modifierTypes)
    }
    
    @JvmStatic
    fun resolveModifierCategory(value: String?, configGroup: CwtConfigGroup): Map<String, CwtModifierCategoryConfig> {
        val finalValue = value ?: "economic_unit" //default to economic_unit
        val enumConfig = configGroup.enums.getValue("scripted_modifier_categories")
        var keys = getModifierCategoryOptionValues(enumConfig, finalValue)
        if(keys == null) keys = getModifierCategoryOptionValues(enumConfig, "economic_unit")
        if(keys == null) keys = emptySet() //unexpected
        val modifierCategories = configGroup.modifierCategories
        return keys.associateWith { modifierCategories.getValue(it) }
    }
    
    @JvmStatic
    fun getModifierCategoryOptionValues(enumConfig: CwtEnumConfig, finalValue: String): Set<String>? {
        val valueConfig = enumConfig.valueConfigMap.getValue(finalValue)
        return valueConfig.getOrPutUserData(modifierCategoriesKey) {
            valueConfig.options?.find { it.key == "modifier_categories" }
                ?.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue }?.takeIfNotEmpty()
        }
    }
}