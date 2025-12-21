package icu.windea.pls.ep.config

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.annotations.WithGameTypeEP

/**
 * 用于在解析了成员规则后，执行额外的处理逻辑。
 */
@WithGameTypeEP
interface CwtConfigPostProcessor {
    /**
     * 执行额外的处理逻辑。
     *
     * @param config 目标成员规则。
     * @return 是否继续执行。
     */
    fun postProcess(config: CwtMemberConfig<*>): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtConfigPostProcessor>("icu.windea.pls.configPostProcessor")
    }
}
