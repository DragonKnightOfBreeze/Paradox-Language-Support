package icu.windea.pls.lang

import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.StatefulValue
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate

object PlsKeys : KeyRegistry() {
    /** 用于在文件级别保存语言环境规则（[CwtLocaleConfig]）。 */
    val cachedLocaleConfig by registerKey<StatefulValue<CwtLocaleConfig>>(this)
}
