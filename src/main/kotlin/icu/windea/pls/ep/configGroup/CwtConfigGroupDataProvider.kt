package icu.windea.pls.ep.configGroup

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupInitializer

/**
 * 用于初始化规则分组中的数据。
 */
interface CwtConfigGroupDataProvider {
    /**
     * 执行处理流程。
     *
     * 说明：
     * - 通过 `configGroup.initializer` 访问和修改正在初始化的规则数据。
     * - 不应直接通过 [configGroup] 访问和修改规则数据，这可能为空或者已过时。
     */
    suspend fun process(initializer: CwtConfigGroupInitializer, configGroup: CwtConfigGroup)

    /**
     * 执行后续优化。
     *
     * 说明：
     * - 通过 `configGroup.initializer` 访问和修改正在初始化的规则数据。
     * - 不应直接通过 [configGroup] 访问和修改规则数据，这可能为空或者已过时。
     */
    suspend fun postOptimize(configGroup: CwtConfigGroup) {}

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigGroupDataProvider>("icu.windea.pls.configGroupDataProvider")
    }
}
