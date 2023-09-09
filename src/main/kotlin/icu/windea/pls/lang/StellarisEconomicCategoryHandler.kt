package icu.windea.pls.lang

import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.data.impl.StellarisEconomicCategoryDataProvider.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.lang.invoke.*

@WithGameType(ParadoxGameType.Stellaris)
object StellarisEconomicCategoryHandler {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
    val cachedEconomicCategoryInfoKey = Key.create<CachedValue<StellarisEconomicCategoryInfo>>("paradox.cached.economicCategoryInfo")
    val modifierCategoriesKey = Key.create<Set<String>>("stellaris.economicCategory.modifierCategories")
    
    /**
     * 输入[definition]的定义类型应当保证是`economic_category`。
     */
    fun getInfo(definition: ParadoxScriptDefinitionElement): StellarisEconomicCategoryInfo? {
        if(selectGameType(definition) != ParadoxGameType.Stellaris) return null
        return doGetInfoFromCache(definition)
    }
    
    private fun doGetInfoFromCache(definition: ParadoxScriptDefinitionElement): StellarisEconomicCategoryInfo? {
        if(definition !is ParadoxScriptProperty) return null
        return CachedValuesManager.getCachedValue(definition, cachedEconomicCategoryInfoKey) {
            ProgressManager.checkCanceled()
            val value = runReadAction { doGetInfo(definition) }
            CachedValueProvider.Result.create(value, definition)
        }
    }
    
    private fun doGetInfo(definition: ParadoxScriptProperty): StellarisEconomicCategoryInfo? {
        //这种写法可能存在一定性能问题，但是问题不大
        //兼容继承的mult修正
        try {
            val name = definition.name.orNull() ?: return null
            val resources = getResources(definition).orNull() ?: return null //unexpected
            val data = definition.getData<Data>() ?: return null
            val parentDataMap = collectParentData(definition, data)
            
            val useForAiBudget = data.useForAiBudget
            val useForAiBudgetForMult = parentDataMap.values.all { it.useForAiBudget }
            
            val parents = parentDataMap.keys
            val modifiers = mutableSetOf<StellarisEconomicCategoryModifierInfo>()
            
            // will generate where use_for_ai_budget = yes (inherited by parent property for _mult modifiers)
            // <economic_category>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
            // will generate:
            // <economic_category>_<resource>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
            
            fun addModifier(key: String, category: String, type: String, triggered: Boolean, useParentIcon: Boolean) {
                if(useForAiBudget || (type == "mult" && useForAiBudgetForMult)) {
                    modifiers.add(StellarisEconomicCategoryModifierInfo(key, null, category, type, triggered, useParentIcon))
                }
                resources.forEach { resource ->
                    modifiers.add(StellarisEconomicCategoryModifierInfo(key, resource, category, type, triggered, useParentIcon))
                }
            }
            
            data.generateAddModifiers.forEach { category ->
                addModifier(name, category, "add", false, false)
            }
            data.generateMultModifiers.forEach { category ->
                addModifier(name, category, "mult", false, false)
            }
            
            data.triggeredProducesModifiers.forEach {
                it.modifierTypes.forEach { type ->
                    addModifier(it.key, "produces", type, true, it.useParentIcon)
                }
            }
            data.triggeredCostModifiers.forEach {
                it.modifierTypes.forEach { type ->
                    addModifier(it.key, "cost", type, true, it.useParentIcon)
                }
            }
            data.triggeredUpkeepModifiers.forEach {
                it.modifierTypes.forEach { type ->
                    addModifier(it.key, "upkeep", type, true, it.useParentIcon)
                }
            }
            
            return StellarisEconomicCategoryInfo(name, data.parent, useForAiBudget, data.modifierCategory, parents, modifiers)
        } catch(e: Exception) {
            if(e is ProcessCanceledException) throw e
            logger.error(e)
            return null
        }
    }
    
    private fun getResources(contextElement: PsiElement): Set<String> {
        val selector = definitionSelector(contextElement.project, contextElement)
        return ParadoxDefinitionSearch.search("resource", selector).findAll()
            .mapTo(mutableSetOf()) { it.name }  //it.name is ok
    }
    
    private fun collectParentData(contextElement: PsiElement, data: Data, map: MutableMap<String, Data> = mutableMapOf()): Map<String, Data> {
        val parent = data.parent ?: return map
        withRecursionGuard("icu.windea.pls.lang.StellarisEconomicCategoryHandler.collectParentData") {
            withCheckRecursion(parent) {
                val selector = definitionSelector(contextElement.project, contextElement).contextSensitive()
                ParadoxDefinitionSearch.search(parent, "economic_category", selector).processQuery p@{
                    ProgressManager.checkCanceled()
                    val parentData = it.getData<Data>() ?: return@p true
                    map.put(parent, parentData)
                    collectParentData(contextElement, parentData, map)
                    true
                }
            }
        }
        return map
    }
    
    fun resolveModifierCategory(value: String?, configGroup: CwtConfigGroup): Map<String, CwtModifierCategoryConfig> {
        val finalValue = value ?: "economic_unit" //default to economic_unit
        val enumConfig = configGroup.enums.getValue("scripted_modifier_category")
        var keys = getModifierCategoryOptionValues(enumConfig, finalValue)
        if(keys == null) keys = getModifierCategoryOptionValues(enumConfig, "economic_unit")
        if(keys == null) keys = emptySet() //unexpected
        val modifierCategories = configGroup.modifierCategories
        return keys.associateWith { modifierCategories.getValue(it) }
    }
    
    private fun getModifierCategoryOptionValues(enumConfig: CwtEnumConfig, finalValue: String): Set<String>? {
        val valueConfig = enumConfig.valueConfigMap.getValue(finalValue)
        return valueConfig.getOrPutUserData(modifierCategoriesKey, emptySet()) {
            valueConfig.findOption("modifier_categories")?.getOptionValues()
        }
    }
}
