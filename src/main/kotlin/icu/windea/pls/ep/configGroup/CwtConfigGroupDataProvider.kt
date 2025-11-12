package icu.windea.pls.ep.configGroup

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupInitializer

/**
 * 用于获取规则分组中的数据。
 */
interface CwtConfigGroupDataProvider {
    /**
     * 执行处理流程。
     *
     * @param initializer 规则分组的初始化器，用于在初始化时存储规则数据。
     * @param configGroup 最终使用的规则分组，在初始化时不应直接更改规则数据。
     */
    suspend fun process(initializer: CwtConfigGroupInitializer, configGroup: CwtConfigGroup)

    /**
     * 执行后续优化。
     *
     * @param initializer 规则分组的初始化器，用于在初始化时存储规则数据。
     * @param configGroup 最终使用的规则分组，在初始化时不应直接更改规则数据。
     */
    suspend fun postOptimize(initializer: CwtConfigGroupInitializer, configGroup: CwtConfigGroup) {}

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigGroupDataProvider>("icu.windea.pls.configGroupDataProvider")
    }
}

