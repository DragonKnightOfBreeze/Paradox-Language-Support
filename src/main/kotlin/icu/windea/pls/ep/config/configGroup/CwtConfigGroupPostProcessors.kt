package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.progress.checkCanceled
import icu.windea.pls.config.attributes.CwtInlinedConfigAttributesEvaluator
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupDataHolderBase
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class CwtBaseDataConfigGroupPostProcessor : CwtConfigGroupPostProcessor {
    override suspend fun postProcess(configGroup: CwtConfigGroup) {
        // load lazy data for various configs
        loadLazyData(configGroup)

        // load lazy data async for various configs
        loadLazyDataAsync(configGroup)
    }

    private suspend fun loadLazyData(configGroup: CwtConfigGroup) {
        checkCanceled()
        configGroup.singleAliases.forEach { (k, v) ->
            configGroup.singleAliasAttributes[k] = CwtInlinedConfigAttributesEvaluator().evaluate(k, v, configGroup)
        }
        if (configGroup is CwtConfigGroupDataHolderBase) configGroup.singleAliasAttributes.trim()

        checkCanceled()
        configGroup.aliasGroups.forEach { (k, v) ->
            configGroup.aliasAttributes[k] = CwtInlinedConfigAttributesEvaluator().evaluate(k, v.values, configGroup)
        }
        if (configGroup is CwtConfigGroupDataHolderBase) configGroup.aliasAttributes.trim()

        checkCanceled()
        configGroup.types.values.forEach { it.attributes }

        checkCanceled()
        configGroup.declarations.values.forEach { it.attributes }
    }

    private suspend fun loadLazyDataAsync(configGroup: CwtConfigGroup) {
        checkCanceled()
        coroutineScope {
            launch {
                configGroup.declarations.values.forEach { it.configForDeclaration }
            }
            launch {
                configGroup.complexEnums.values.forEach { it.enumNameConfigs }
            }
        }
    }
}
