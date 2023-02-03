package icu.windea.pls.config.core

import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.script.*

@WithGameType(ParadoxGameType.Stellaris)
object ParadoxEconomicCategoryHandler {
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
        val data = ParadoxScriptDataResolver.resolveProperty(definition) ?: return null
        return null
    }
}