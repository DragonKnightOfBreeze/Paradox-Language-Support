package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.internal.impl.CwtSchemaConfigResolverImpl
import icu.windea.pls.config.configGroup.CwtConfigGroup

data class CwtSchemaConfig(
    val file: CwtFileConfig,
    val properties: List<CwtPropertyConfig>,
    val enums: Map<String, CwtPropertyConfig>,
    val constraints: Map<String, CwtPropertyConfig>
) : CwtDetachedConfig {
    interface Resolver {
        fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup)
    }

    companion object : Resolver by CwtSchemaConfigResolverImpl()
}
