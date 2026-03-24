package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 用于在初始化规则分组时，执行特定的处理逻辑。
 *
 * 此时规则分组数据尚未真正加入规则分组，需要通过规则分组的初始化器访问正在初始化的规则分组数据。
 */
interface CwtConfigGroupProcessor {
    /**
     * 在初始化规则分组时，依次执行的处理逻辑。
     *
     * 说明：
     * - 通过 `configGroup.initializer` 访问和修改正在初始化的规则数据。
     * - 不应直接通过 `configGroup` 访问和修改规则数据，这可能为空或者已过时。
     */
    suspend fun process(configGroup: CwtConfigGroup) {}

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigGroupProcessor>("icu.windea.pls.configGroupProcessor")
    }
}
