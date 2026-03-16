package icu.windea.pls.ep.config.configGroup

import icu.windea.pls.config.configGroup.CwtConfigGroup
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * 用于初始化规则分组中需要最后加载的那些数据。
 */
class CwtPostConfigGroupDataProvider : CwtConfigGroupDataProvider {
    override suspend fun process(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer

        // load lazy data async for various configs
        coroutineScope {
            for (typeConfig in initializer.types.values) {
                launch {
                    typeConfig.possibleTypeKeys
                }
            }
            for (declarationConfig in initializer.declarations.values) {
                launch {
                    declarationConfig.attributes
                    declarationConfig.configForDeclaration
                }
            }
            for (complexEnumConfig in initializer.complexEnums.values) {
                launch {
                    complexEnumConfig.enumNameConfigs
                }
            }
        }
    }

    override suspend fun postProcess(configGroup: CwtConfigGroup) {
        // 2.0.7 nothing now (since it's not very necessary)
    }
}
