package icu.windea.pls.ep.config

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.annotations.WithGameTypeEP

/**
 * 用于在解析了成员规则后，执行额外的处理逻辑。
 */
@WithGameTypeEP
interface CwtConfigPostProcessor {
    fun supports(config: CwtMemberConfig<*>): Boolean = true

    fun deferred(config: CwtMemberConfig<*>): Boolean = false

    fun postProcess(config: CwtMemberConfig<*>)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigPostProcessor>("icu.windea.pls.configPostProcessor")
    }
}
