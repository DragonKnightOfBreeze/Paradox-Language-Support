package icu.windea.pls.ep.config.config

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig

/**
 * 用于在成员规则解析完毕后，执行额外的处理逻辑。
 */
interface CwtConfigPostProcessor {
    fun supports(config: CwtMemberConfig<*>): Boolean = true

    fun deferred(config: CwtMemberConfig<*>): Boolean = false

    fun postProcess(config: CwtMemberConfig<*>)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigPostProcessor>("icu.windea.pls.configPostProcessor")
    }
}
