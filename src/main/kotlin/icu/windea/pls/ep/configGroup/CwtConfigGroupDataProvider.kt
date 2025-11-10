package icu.windea.pls.ep.configGroup

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 用于获取规则分组中的数据。
 */
interface CwtConfigGroupDataProvider {
    /**
     * @param configGroupOnInit 初始化时用于存放规则数据的规则分组。在初始化完成后会被清除。
     * @param configGroup 最终使用的规则分组，在初始化时不应直接更改规则数据。
     * @return 是否继续遍历。
     */
    suspend fun process(configGroupOnInit: CwtConfigGroup, configGroup: CwtConfigGroup): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigGroupDataProvider>("icu.windea.pls.configGroupDataProvider")
    }
}

