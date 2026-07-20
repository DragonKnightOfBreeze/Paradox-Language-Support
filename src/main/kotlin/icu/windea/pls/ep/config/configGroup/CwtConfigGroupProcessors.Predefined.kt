package icu.windea.pls.ep.config.configGroup

import icu.windea.pls.base.data.ChronicleJsonService
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于初始化规则分组中预先定义的那些数据。
 *
 * 读取并加入来自 `/data/config_group_data_list.json5` 的 JSON 数据。
 */
class CwtPredefinedConfigGroupProcessor : CwtConfigGroupProcessor {
    override suspend fun process(configGroup: CwtConfigGroup) {
        processJsonData(configGroup)
    }

    private fun processJsonData(configGroup: CwtConfigGroup) {
        val initializer = configGroup.initializer
        val jsonData = ChronicleJsonService.configGroupDataList
        val jsonList = jsonData.filter { it.gameType == ParadoxGameType.Core || it.gameType == configGroup.gameType }
        for (json in jsonList) {
            initializer.aliasNamesSupportScope += json.aliasNamesSupportScope

            val typesModel = initializer.typesModel
            typesModel.supportScope += json.typesSupportScope
            typesModel.indirectSupportScope += json.typesIndirectSupportScope
            typesModel.skipCheckSystemScope += json.typesSkipCheckSystemScope
            typesModel.supportParameters += json.typesSupportParameters
            typesModel.supportScopeContextInference += json.typesSupportScopeContextInference
        }
    }
}
