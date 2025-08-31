package icu.windea.pls.config.config.internal.impl

import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.schemas

internal class CwtSchemaConfigResolverImpl : CwtSchemaConfig.Resolver {
    fun resolveInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        val properties = mutableListOf<CwtPropertyConfig>()
        val enums = mutableMapOf<String, CwtPropertyConfig>()
        val constraints = mutableMapOf<String, CwtPropertyConfig>()
        fileConfig.properties.forEach { prop ->
            val keyExpression = CwtSchemaExpression.resolve(prop.key)
            when (keyExpression) {
                is CwtSchemaExpression.Enum -> enums[keyExpression.name] = prop
                is CwtSchemaExpression.Constraint -> constraints[keyExpression.name] = prop
                else -> properties += prop
            }
        }
        val schemaConfig = CwtSchemaConfig(fileConfig, properties, enums, constraints)
        configGroup.schemas += schemaConfig
    }
}
