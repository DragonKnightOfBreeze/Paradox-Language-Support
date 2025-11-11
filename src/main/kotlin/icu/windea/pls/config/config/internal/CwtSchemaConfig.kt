package icu.windea.pls.config.config.internal

import icu.windea.pls.config.config.CwtDetachedConfig
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.internal.impl.CwtSchemaConfigResolverImpl
import icu.windea.pls.config.configGroup.CwtConfigGroupInitializer

/**
 * 作为规则本身的架构（schema）的内部规则，不支持自定义。
 *
 * 用于验证规则的格式与结构。事实上，基于这些规则，PLS 目前仅提供了适用于规则文件的初步的代码补全。
 *
 * @see icu.windea.pls.config.configExpression.CwtSchemaExpression
 * @see icu.windea.pls.config.util.CwtConfigSchemaManager
 * @see icu.windea.pls.lang.codeInsight.completion.CwtConfigCompletionManager
 */
data class CwtSchemaConfig(
    val file: CwtFileConfig,
    val properties: List<CwtPropertyConfig>,
    val enums: Map<String, CwtPropertyConfig>,
    val constraints: Map<String, CwtPropertyConfig>
) : CwtDetachedConfig {
    interface Resolver {
        fun resolveInFile(initializer: CwtConfigGroupInitializer, fileConfig: CwtFileConfig)
    }

    companion object : Resolver by CwtSchemaConfigResolverImpl()
}
