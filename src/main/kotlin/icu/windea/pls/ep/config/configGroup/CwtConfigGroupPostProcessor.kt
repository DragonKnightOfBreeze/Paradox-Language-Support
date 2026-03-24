package icu.windea.pls.ep.config.configGroup

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 用于在规则分组初始化完毕后，执行特定的处理逻辑。
 *
 * 此时规则分组数据已经真正加入规则分组，而规则分组的初始化器中的数据已被清空。
 */
interface CwtConfigGroupPostProcessor {
    /**
     * 在规则分组初始化完毕后，依次执行的处理逻辑。
     */
    suspend fun postProcess(configGroup: CwtConfigGroup)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigGroupPostProcessor>("icu.windea.pls.configGroupPostProcessor")
    }
}
