package icu.windea.pls.config.config.internal.impl

import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.config.util.CwtConfigResolverScope

internal class CwtSchemaConfigResolverImpl : CwtSchemaConfig.Resolver, CwtConfigResolverScope {
    // no logger here (unnecessary)

    override fun resolveInFile(fileConfig: CwtFileConfig) {
        val initializer = fileConfig.configGroup.initializer
        val properties = mutableListOf<CwtPropertyConfig>()
        val enums = mutableMapOf<String, CwtPropertyConfig>()
        val constraints = mutableMapOf<String, CwtPropertyConfig>()
        for (prop in fileConfig.properties) {
            val keyExpression = CwtSchemaExpression.resolve(prop.key)
            when (keyExpression) {
                is CwtSchemaExpression.Enum -> enums[keyExpression.name] = prop
                is CwtSchemaExpression.Constraint -> constraints[keyExpression.name] = prop
                else -> properties += prop
            }
        }
        val schemaConfig = CwtSchemaConfig(fileConfig, properties, enums, constraints)
        initializer.schemas += schemaConfig
    }
}
