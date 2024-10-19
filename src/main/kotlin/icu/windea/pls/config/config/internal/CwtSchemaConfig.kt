package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.internal.*

class CwtSchemaConfig(
    val properties: List<CwtPropertyConfig>,
    val enums: Map<String, CwtPropertyConfig>,
    val constraints: Map<String, CwtPropertyConfig>
) : CwtDetachedConfig {
    companion object Resolver {
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
            val schemaConfig = CwtSchemaConfig(properties, enums, constraints)
            configGroup.schemas += schemaConfig
        }
    }
}
