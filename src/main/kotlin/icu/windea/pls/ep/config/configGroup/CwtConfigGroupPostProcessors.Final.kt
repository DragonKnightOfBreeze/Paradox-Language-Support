package icu.windea.pls.ep.config.configGroup

import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.model.CwtConfigGroupDataModelBase

class CwtFinalConfigGroupPostProcessor : CwtConfigGroupPostProcessor {
    // NOTE 2.1.7 为了优化内存，最终需要整理规则分组数据占用的内存空间

    override suspend fun postProcess(configGroup: CwtConfigGroup) {
        val dataModel = configGroup.dataModel
        if (dataModel !is CwtConfigGroupDataModelBase) return

        dataModel.trim()
    }
}
