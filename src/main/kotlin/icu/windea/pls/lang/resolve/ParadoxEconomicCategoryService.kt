package icu.windea.pls.lang.resolve

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.util.Processor
import icu.windea.pls.base.annotations.WithGameType
import icu.windea.pls.core.orNull
import icu.windea.pls.core.process
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.ep.util.data.StellarisEconomicCategoryData
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.model.ParadoxEconomicCategoryInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptProperty

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxEconomicCategoryService {
    fun resolveInfo(definition: ParadoxScriptProperty): ParadoxEconomicCategoryInfo? {
        try {
            val info = definition.definitionInfo ?: return null
            if (info.gameType != ParadoxGameType.Stellaris) return null // check game type
            if (info.type != ParadoxDefinitionTypes.economicCategory) return null // check definition type
            val name = info.name.orNull() ?: return null
            val data = definition.getDefinitionData<StellarisEconomicCategoryData>() ?: return null
            val resources = getResources(definition)
            val modifierInfos = getModifierInfos(data, name, resources)
            return ParadoxEconomicCategoryInfo(name, data.parent, data.useForAiBudget, data.modifierCategory, modifierInfos)
        } catch (e: Exception) {
            if (e is ProcessCanceledException) throw e
            thisLogger().error(e)
            return null
        }
    }

    fun getResources(contextElement: PsiElement): Set<String> {
        val selector = ParadoxDefinitionSearch.selector(contextElement.project, contextElement)
        val elements = ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.resource, selector).findAll() // should be ordered
        val result = elements.mapNotNullTo(mutableSetOf()) { it.name.orNull() } // `it.name` is ok
        if (result.isEmpty()) return emptySet()
        return result
    }

    fun getModifierInfos(data: StellarisEconomicCategoryData, name: String, resources: Set<String>): Set<ParadoxEconomicCategoryInfo.ModifierInfo> {
        // general rules:
        // - actual generated modifiers are based on detailed declaration
        // - actual modifier categories are from property `modifier_category` + `"AI Economy"`, see `enum[scripted_modifier_category]`
        //
        // will generate:
        // - `<economic_category>_<resource>_enum[economic_modifier_category]_enum[economic_modifier_type] = { "AI Economy" }`
        //
        // will generate for `_mult` modifiers:
        // - `<economic_category>_enum[economic_modifier_category]_enum[economic_modifier_type] = { "AI Economy" }`
        //
        // actual allowed values for `enum[economic_modifier_category]_enum[economic_modifier_type]` are:
        // - from property `generate_mult_modifiers`, `generate_add_modifiers`
        // - from property `triggered_produces_modifier`, `triggered_cost_modifier`, `triggered_upkeep_modifier`, `triggered_logistics_modifier`

        val result = mutableSetOf<ParadoxEconomicCategoryInfo.ModifierInfo>()
        resources.forEach { resource ->
            result.collectModifierInfos(data, name, resource)
        }
        result.collectModifierInfos(data, name, null)
        if (result.isEmpty()) return emptySet()
        return result
    }

    private fun MutableSet<ParadoxEconomicCategoryInfo.ModifierInfo>.collectModifierInfos(data: StellarisEconomicCategoryData, name: String, resource: String?) {
        data.generateAddModifiers.forEach { category ->
            collectModifierInfo(name, resource, category, "add", false, false)
        }
        data.generateMultModifiers.forEach { category ->
            collectModifierInfo(name, resource, category, "mult", false, false)
        }

        data.triggeredProducesModifiers.forEach {
            it.modifierTypes.forEach { type ->
                collectModifierInfo(it.key, resource, "produces", type, true, it.useParentIcon)
            }
        }
        data.triggeredCostModifiers.forEach {
            it.modifierTypes.forEach { type ->
                collectModifierInfo(it.key, resource, "cost", type, true, it.useParentIcon)
            }
        }
        data.triggeredUpkeepModifiers.forEach {
            it.modifierTypes.forEach { type ->
                collectModifierInfo(it.key, resource, "upkeep", type, true, it.useParentIcon)
            }
        }
        data.triggeredLogisticsModifiers.forEach {
            it.modifierTypes.forEach { type ->
                collectModifierInfo(it.key, resource, "logistics", type, true, it.useParentIcon)
            }
        }
    }

    private fun MutableSet<ParadoxEconomicCategoryInfo.ModifierInfo>.collectModifierInfo(key: String, resource: String?, category: String, type: String, triggered: Boolean, useParentIcon: Boolean) {
        if (key.isEmpty()) return // skip invalid keys
        if (resource == null && type != "mult") return // if resource is not specified, then it must be a `_mult` modifier
        add(ParadoxEconomicCategoryInfo.ModifierInfo(key, resource, category, type, triggered, useParentIcon))
    }

    @Suppress("unused")
    fun processParentData(contextElement: PsiElement, data: StellarisEconomicCategoryData, processor: Processor<StellarisEconomicCategoryData>): Boolean {
        return processParentDataRecursively(contextElement, data, processor)
    }

    private fun processParentDataRecursively(contextElement: PsiElement, data: StellarisEconomicCategoryData, processor: Processor<StellarisEconomicCategoryData>): Boolean {
        val parent = data.parent?.orNull() ?: return true
        return withRecursionGuard {
            withRecursionCheck(parent) {
                val selector = ParadoxDefinitionSearch.selector(contextElement.project, contextElement).contextSensitive()
                ParadoxDefinitionSearch.searchProperty(parent, ParadoxDefinitionTypes.economicCategory, selector).process p@{
                    ProgressManager.checkCanceled()
                    val parentData = it.getDefinitionData<StellarisEconomicCategoryData>() ?: return@p true
                    processor.process(parentData)
                    processParentDataRecursively(contextElement, parentData, processor)
                }
            }
        } ?: true
    }
}
