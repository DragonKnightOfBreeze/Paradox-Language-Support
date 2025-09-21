package icu.windea.pls.lang.util

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.orNull
import icu.windea.pls.core.processQuery
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.data.StellarisEconomicCategoryData
import icu.windea.pls.lang.getData
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxEconomicCategoryInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxEconomicCategoryManager {
    object Keys : KeyRegistry() {
        val cachedEconomicCategoryInfo by createKey<CachedValue<ParadoxEconomicCategoryInfo>>(Keys)
        val modifierCategories by createKey<Set<String>>(Keys)
    }

    private val logger = logger<ParadoxEconomicCategoryManager>()

    /**
     * 输入[definition]的定义类型应当保证是`economic_category`。
     */
    fun getInfo(definition: ParadoxScriptDefinitionElement): ParadoxEconomicCategoryInfo? {
        if (selectGameType(definition) != ParadoxGameType.Stellaris) return null
        return doGetInfoFromCache(definition)
    }

    private fun doGetInfoFromCache(definition: ParadoxScriptDefinitionElement): ParadoxEconomicCategoryInfo? {
        if (definition !is ParadoxScriptProperty) return null
        return CachedValuesManager.getCachedValue(definition, Keys.cachedEconomicCategoryInfo) {
            ProgressManager.checkCanceled()
            val value = runReadAction { doGetInfo(definition) }
            value.withDependencyItems(definition)
        }
    }

    private fun doGetInfo(definition: ParadoxScriptProperty): ParadoxEconomicCategoryInfo? {
        //这种写法可能存在一定性能问题，但是问题不大
        //兼容继承的mult修正
        try {
            val name = definition.name.orNull() ?: return null
            val resources = getResources(definition).orNull() ?: return null //unexpected
            val data = definition.getData<StellarisEconomicCategoryData>() ?: return null
            val parentDataMap = collectParentData(definition, data)

            val useForAiBudget = data.useForAiBudget
            val useForAiBudgetForMult = parentDataMap.values.any { it.useForAiBudget }

            val parents = parentDataMap.keys
            val modifiers = mutableSetOf<ParadoxEconomicCategoryInfo.ModifierInfo>()

            // will generate when use_for_ai_budget = yes (inherited by parent property for _mult modifiers)
            // <economic_category>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }
            // will generate:
            // <economic_category>_<resource>_enum[economic_modifier_categories]_enum[economic_modifier_types] = { "AI Economy" }

            fun addModifier(key: String, category: String, type: String, triggered: Boolean, useParentIcon: Boolean) {
                if (key.isEmpty()) return //skip invalid keys
                if (useForAiBudget || (type == "mult" && useForAiBudgetForMult)) {
                    modifiers.add(ParadoxEconomicCategoryInfo.ModifierInfo(key, null, category, type, triggered, useParentIcon))
                }
                resources.forEach { resource ->
                    modifiers.add(ParadoxEconomicCategoryInfo.ModifierInfo(key, resource, category, type, triggered, useParentIcon))
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

            return ParadoxEconomicCategoryInfo(name, data.parent, useForAiBudget, data.modifierCategory, parents, modifiers)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            logger.error(e)
            return null
        }
    }

    private fun getResources(contextElement: PsiElement): Set<String> {
        val selector = selector(contextElement.project, contextElement).definition()
        return ParadoxDefinitionSearch.search(null, ParadoxDefinitionTypes.Resource, selector).findAll()
            .mapTo(mutableSetOf()) { it.name }  //it.name is ok
    }

    private fun collectParentData(contextElement: PsiElement, data: StellarisEconomicCategoryData, map: MutableMap<String, StellarisEconomicCategoryData> = mutableMapOf()): Map<String, StellarisEconomicCategoryData> {
        val parent = data.parent ?: return map
        withRecursionGuard {
            withRecursionCheck(parent) {
                val selector = selector(contextElement.project, contextElement).definition().contextSensitive()
                ParadoxDefinitionSearch.search(parent, ParadoxDefinitionTypes.EconomicCategory, selector).processQuery p@{
                    ProgressManager.checkCanceled()
                    val parentData = it.getData<StellarisEconomicCategoryData>() ?: return@p true
                    map.put(parent, parentData)
                    collectParentData(contextElement, parentData, map)
                    true
                }
            }
        }
        return map
    }
}
