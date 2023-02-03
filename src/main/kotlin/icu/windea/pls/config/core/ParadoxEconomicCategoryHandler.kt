package icu.windea.pls.config.core

import com.intellij.openapi.diagnostic.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*
import org.jetbrains.kotlin.idea.gradleTooling.*
import java.lang.invoke.*

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxEconomicCategoryHandler {
    private val logger = Logger.getInstance(MethodHandles.lookup().lookupClass())
    
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
        try {
            val data = ParadoxScriptDataResolver.resolveProperty(definition) ?: return null
            val name = definition.name.takeIfNotEmpty() ?: return null
            val parent = data.getValue("parent", valid = true)?.stringValue()
            val useForAiBudget = data.getValue("use_for_ai_budget")?.booleanValue() ?: false
            val generateAddModifiers = data.getValue("generate_add_modifiers", valid = true)
            val modifiers = emptySet<ParadoxEconomicCategoryModifierInfo>()
            val modifierCategory = data.getValue("modifier_category", valid = true)?.stringValue()
            return ParadoxEconomicCategoryInfo(name, parent, useForAiBudget, modifiers, modifierCategory)
        } catch(e: Exception) {
            logger.error(e)
            return null
        }
    }
}