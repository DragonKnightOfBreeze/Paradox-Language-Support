package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.internal.impl.CwtFoldingSettingsConfigResolverImpl
import icu.windea.pls.config.configGroup.CwtConfigGroup

data class CwtFoldingSettingsConfig(
    val id: String,
    val key: String?,
    val keys: List<String>?,
    val placeholder: String
) : CwtDetachedConfig {
    interface Resolver {
        fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup)
    }

    companion object : Resolver by CwtFoldingSettingsConfigResolverImpl()
}
