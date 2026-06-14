package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.CwtConfigResolverScope
import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.config.util.CwtConfigSchemaManager

/**
 * 作为规则自身的模式（schema）的内部规则。不支持自定义。
 *
 * 用于验证规则自身的格式与结构。
 *
 * 事实上，基于这些规则，插件目前仅提供了适用于规则文件的初步的代码补全。
 *
 * @see CwtSchemaExpression
 * @see CwtConfigSchemaManager
 */
data class CwtSchemaConfig(
    val file: CwtFileConfig,
    val properties: List<CwtPropertyConfig>,
    val enums: Map<String, CwtPropertyConfig>,
    val constraints: Map<String, CwtPropertyConfig>
) : CwtDetachedConfig {
    companion object {
        @JvmStatic
        fun resolveInFile(fileConfig: CwtFileConfig) {
            return CwtSchemaConfigResolver.resolveInFile(fileConfig)
        }
    }
}

// region Implementations

private object CwtSchemaConfigResolver : CwtConfigResolverScope {
    // private val logger = thisLogger()

    fun resolveInFile(fileConfig: CwtFileConfig) {
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

// endregion
