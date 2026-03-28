package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configExpression.CwtSchemaExpression
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.CwtConfigSchemaManager
import icu.windea.pls.lang.codeInsight.completion.CwtConfigCompletionManager

/**
 * 作为规则自身的模式（schema）的内部规则。不支持自定义。
 *
 * 用于验证规则自身的格式与结构。
 *
 * 事实上，基于这些规则，插件目前仅提供了适用于规则文件的初步的代码补全。
 *
 * @see CwtSchemaExpression
 * @see CwtConfigSchemaManager
 * @see CwtConfigCompletionManager
 */
data class CwtSchemaConfig(
    val file: CwtFileConfig,
    val properties: List<CwtPropertyConfig>,
    val enums: Map<String, CwtPropertyConfig>,
    val constraints: Map<String, CwtPropertyConfig>
) : CwtDetachedConfig {
    interface Resolver {
        fun resolveInFile(fileConfig: CwtFileConfig)
    }

    companion object : Resolver by CwtSchemaConfigResolverImpl()
}

// region Implementations

private class CwtSchemaConfigResolverImpl : CwtSchemaConfig.Resolver, CwtConfigResolverScope {
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

// endregion
