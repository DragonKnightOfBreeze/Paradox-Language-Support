package icu.windea.pls.ep.configGroup

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupInitializer

/**
 * 用于获取规则分组中的数据。
 */
interface CwtConfigGroupDataProvider {
    /**
     * @param initializer 规则分组的初始化器，用于在初始化时存储规则数据。
     * @param configGroup 最终使用的规则分组，在初始化时不应直接更改规则数据。
     * @return 是否继续遍历。
     */
    suspend fun process(initializer: CwtConfigGroupInitializer, configGroup: CwtConfigGroup): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigGroupDataProvider>("icu.windea.pls.configGroupDataProvider")
    }
}

