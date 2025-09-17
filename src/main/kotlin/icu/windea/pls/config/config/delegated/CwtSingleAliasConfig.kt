package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSingleAliasConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 单别名规则。
 *
 * 单别名规则是一种可以按一对一的形式，在多个位置复用（作为属性的值）的规则。
 * 单别名规则可用来简化规则文件，提升可读性和复用性。
 * 另外，包括触发块（trigger clause）、效应块（effect clause）在内的多种代码片段对应的规则，都建议以单别名规则的形式提供。
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
 * @property name 名称。
 */
interface CwtSingleAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("single_alias[$]")
    val name: String

    /** 将该单别名内联展开为普通属性规则。*/
    fun inline(config: CwtPropertyConfig): CwtPropertyConfig

    interface Resolver {
        /** 由属性规规则解析为单别名规则。*/
        fun resolve(config: CwtPropertyConfig): CwtSingleAliasConfig?
    }

    companion object : Resolver by CwtSingleAliasConfigResolverImpl()
}
