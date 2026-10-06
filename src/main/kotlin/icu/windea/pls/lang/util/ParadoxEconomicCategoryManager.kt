package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.lang.resolve.ParadoxEconomicCategoryService
import icu.windea.pls.lang.resolve.ParadoxModifierCategoryService
import icu.windea.pls.model.ParadoxEconomicCategoryInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty

@ForGameType(ParadoxGameType.Stellaris)
object ParadoxEconomicCategoryManager {
    object Keys : KeyRegistry() {
        val cachedEconomicCategoryInfo by registerKey<CachedValue<ParadoxEconomicCategoryInfo>>(Keys)
    }

    fun getInfo(definition: ParadoxScriptProperty): ParadoxEconomicCategoryInfo? {
        return getInfoFromCache(definition)
    }

    private fun getInfoFromCache(definition: ParadoxScriptProperty): ParadoxEconomicCategoryInfo? {
        return CachedValuesManager.getCachedValue(definition, Keys.cachedEconomicCategoryInfo) {
            ProgressManager.checkCanceled()
            val value = ParadoxEconomicCategoryService.resolveInfo(definition)
            CachedValueProvider.Result.create(value, definition)
        }
    }

    fun getModifierCategories(value: String?, configGroup: CwtConfigGroup): Map<String, CwtModifierCategoryConfig> {
        // get from config
        val result = ParadoxModifierCategoryService.getModifierCategoriesFromConfig(value, configGroup)
        if (result.isNotEmpty()) return result
        // fallback: default to `economic_unit`
        return ParadoxModifierCategoryService.getModifierCategoriesFromConfig("economic_unit", configGroup)
    }
}
