package icu.windea.pls.config.config.internal.impl

import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.data.schemas
import icu.windea.pls.config.util.CwtConfigResolverMixin

internal class CwtSchemaConfigResolverImpl : CwtSchemaConfig.Resolver, CwtConfigResolverMixin {
    // no logger here (unnecessary)

    override fun resolveInFile(configGroupOnInit: CwtConfigGroup, fileConfig: CwtFileConfig) {
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
        configGroupOnInit.schemas += schemaConfig
    }
}
