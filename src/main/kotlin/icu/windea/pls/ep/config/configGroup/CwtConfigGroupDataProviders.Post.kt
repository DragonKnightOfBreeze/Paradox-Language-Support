package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.progress.checkCanceled
import icu.windea.pls.config.attributes.CwtInlinedConfigAttributesEvaluator
import icu.windea.pls.config.configGroup.CwtConfigGroup
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * 用于初始化规则分组中需要最后加载的那些数据。
 */
class CwtPostConfigGroupDataProvider : CwtConfigGroupDataProvider {

    override suspend fun postProcess(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer

        // load lazy data for various configs
        run {
            checkCanceled()
            initializer.singleAliases.forEach { (k, v) ->
                initializer.singleAliasAttributes[k] = CwtInlinedConfigAttributesEvaluator.evaluate(k, v, initializer)
            }
            checkCanceled()
            initializer.aliasGroups.forEach { (k, v) ->
                initializer.aliasAttributes[k] = CwtInlinedConfigAttributesEvaluator.evaluate(k, v.values, initializer)
            }
            checkCanceled()
            initializer.types.values.forEach { it.attributes }
            checkCanceled()
            initializer.declarations.values.forEach { it.attributes }
        }

        // load lazy data async for various configs
        coroutineScope {
            launch {
                initializer.declarations.values.forEach { it.configForDeclaration }
            }
            launch {
                initializer.complexEnums.values.forEach { it.enumNameConfigs }
            }
        }
    }
}
