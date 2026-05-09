package icu.windea.pls.ep.config.config

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtConfig

/**
 * 用于过滤要加入规则分组数据的规则。
 *
 * 说明：
 * - 仅限非内部规则。
 */
interface CwtConfigFilterProvider {
    /**
     * @return 是否需要过滤掉，不再加入规则分组数据。
     */
    fun filter(config: CwtConfig<*>): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigFilterProvider>("icu.windea.pls.configFilterProvider")
    }
}
