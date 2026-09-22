package icu.windea.pls.ep.config.configGroup

import icu.windea.pls.base.ChronicleCapacities
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 用于在初始化规则分组时，进行最终的清理工作。
 */
class CwtFinalConfigGroupProcessor : CwtConfigGroupProcessor {
    // NOTE 2.1.5 为了优化内存，文件规则最终默认不会保留在规则分组数据中

    override suspend fun process(configGroup: CwtConfigGroup) {
        val keepFileConfigs = ChronicleCapacities.keepFileConfigs()
        if (!keepFileConfigs) {
            val fileConfigs = configGroup.initializer.fileConfigs
            fileConfigs.clear()
        }

        val configPostProcessActions = configGroup.initializer.configPostProcessActions
        configPostProcessActions.clear()
    }
}
