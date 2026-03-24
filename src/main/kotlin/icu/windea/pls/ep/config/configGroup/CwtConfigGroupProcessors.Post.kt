package icu.windea.pls.ep.config.configGroup

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.CwtConfigResolverManager

/**
 * 用于在初始化规则分组时，进行最终的清理和优化工作。
 */
class CwtPostConfigGroupProcessor : CwtConfigGroupProcessor {
    override suspend fun process(configGroup: CwtConfigGroup) {
        clearData(configGroup)
        trimData(configGroup)
    }

    private fun clearData(configGroup: CwtConfigGroup) {
        // NOTE 2.1.5 为了优化内存，文件规则最终不会保留在规则分组数据中
        val fileConfigs = CwtConfigResolverManager.getFileConfigs(configGroup)
        fileConfigs.clear()

        val postProcessActions = CwtConfigResolverManager.getPostProcessActions(configGroup)
        postProcessActions.clear()
    }

    private fun trimData(configGroup: CwtConfigGroup) {
        // NOTE 2.1.7 为了优化内存，最终需要整理规则分组数据占用的内存空间
        val initializer = configGroup.initializer
        initializer.trim()
    }
}
