package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.internal.impl.CwtFoldingSettingsConfigResolverImpl
import icu.windea.pls.config.configGroup.CwtConfigGroupInitializer

/**
 * 作为折叠设置的内部规则，目前尚不支持自定义。
 *
 * 用于提供额外的代码折叠。
 *
 * @see icu.windea.pls.lang.folding.ParadoxExpressionFoldingBuilder
 */
data class CwtFoldingSettingsConfig(
    val id: String,
    val key: String?,
    val keys: List<String>?,
    val placeholder: String
) : CwtDetachedConfig {
    interface Resolver {
        fun resolveInFile(initializer: CwtConfigGroupInitializer, fileConfig: CwtFileConfig)
    }

    companion object : Resolver by CwtFoldingSettingsConfigResolverImpl()
}
