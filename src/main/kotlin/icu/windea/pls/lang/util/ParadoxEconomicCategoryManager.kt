package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.ParadoxEconomicCategoryService
import icu.windea.pls.model.ParadoxEconomicCategoryInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptProperty

@ForGameType(ParadoxGameType.Stellaris)
object ParadoxEconomicCategoryManager {
    object Keys : KeyRegistry() {
        val cachedEconomicCategoryInfo by registerKey<CachedValue<ParadoxEconomicCategoryInfo>>(Keys)
    }

    fun getInfo(definition: ParadoxScriptProperty): ParadoxEconomicCategoryInfo? {
        // from cache
        return getInfoFromCache(definition)
    }

    private fun getInfoFromCache(definition: ParadoxScriptProperty): ParadoxEconomicCategoryInfo? {
        return CachedValuesManager.getCachedValue(definition, Keys.cachedEconomicCategoryInfo) {
            ProgressManager.checkCanceled()
            runSmartReadAction {
                val value = ParadoxEconomicCategoryService.resolveInfo(definition)
                value.withDependencyItems(definition)
            }
        }
    }

    fun getModifierCategories(value: String?, configGroup: CwtConfigGroup): Map<String, CwtModifierCategoryConfig> {
        val result = ParadoxConfigService.getModifierCategories(value, configGroup)
        if (result.isNotEmpty()) return result
        // fallback: default to `economic_unit`
        return ParadoxConfigService.getModifierCategories("economic_unit", configGroup)
    }
}
