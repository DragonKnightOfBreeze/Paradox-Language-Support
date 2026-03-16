package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 用于处理规则分组数据。
 */
interface CwtConfigGroupDataProvider {
    /**
     * 在初始化规则分组时，执行的处理流程。
     *
     * 说明：
     * - 通过 `configGroup.initializer` 访问和修改正在初始化的规则数据。
     * - 不应直接通过 `configGroup` 访问和修改规则数据，这可能为空或者已过时。
     */
    suspend fun process(configGroup: CwtConfigGroup) {}

    /**
     * 在处理流程（[process]）全部执行完毕后，依次执行的后续优化流程。
     *
     * 说明：
     * - 通过 `configGroup.initializer` 访问和修改正在初始化的规则数据。
     * - 不应直接通过 `configGroup` 访问和修改规则数据，这可能为空或者已过时。
     */
    suspend fun optimize(configGroup: CwtConfigGroup) {}

    /**
     * 在规则分组数据加载完毕，并且真正加入规则分组之后，再依次执行的后续处理流程。
     */
    suspend fun postProcess(configGroup: CwtConfigGroup) {}

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigGroupDataProvider>("icu.windea.pls.configGroupDataProvider")
    }
}
