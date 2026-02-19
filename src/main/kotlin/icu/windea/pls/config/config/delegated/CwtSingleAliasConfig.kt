package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 单别名规则。
 *
 * 单别名规则是一种可以按一对一的形式，在多个位置复用（作为属性的值）的规则。
 * 单别名规则可用来简化规则文件，提升可读性和复用性。
 * 另外，包括触发块（trigger clause）、效果块（effect clause）在内的多种代码片段对应的规则，都建议以单别名规则的形式提供。
 *
 * 路径定位：`single_alias[{name}]`，`{name}` 匹配名称。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * # declaration
 * single_alias[trigger_clause] = {
 *     alias_name[trigger] = alias_match_left[trigger]
 * }
 *
 * # usage
 * army = {
 *     ## cardinality = 0..1
 * 	   potential = single_alias_right[trigger_clause]
 * }
 * ```
 *
 * @property name 名称。
 *
 * @see icu.windea.pls.config.util.manipulators.CwtConfigManipulator.inlineSingleAlias
 */
interface CwtSingleAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("single_alias[$]")
    val name: String

    interface Resolver {
        /** 由属性规则解析为单别名规则。 */
        fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig?
    }

    companion object : Resolver by CwtSingleAliasConfigResolverImpl()
}

// region Implementations

private class CwtSingleAliasConfigResolverImpl : CwtSingleAliasConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSingleAliasConfig? {
        val key = config.key
        val name = key.removeSurroundingOrNull("single_alias[", "]")?.orNull()?.optimized() ?: return null
        logger.debug { "Resolved single alias config (name: $name).".withLocationPrefix(config) }
        return CwtSingleAliasConfigImpl(config, name)
    }
}

private class CwtSingleAliasConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String
) : UserDataHolderBase(), CwtSingleAliasConfig {
    override fun toString() = "CwtSingleAliasConfigImpl(name='$name')"
}

// endregion
