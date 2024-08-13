package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*

data class CwtSchemaConfig(
    val configs: List<CwtPropertyConfig>
): CwtDetachedConfig {
    companion object Resolver {
        fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
            val schemaConfig = CwtSchemaConfig(fileConfig.properties)
            configGroup.schemas += schemaConfig
        }
    }
}
