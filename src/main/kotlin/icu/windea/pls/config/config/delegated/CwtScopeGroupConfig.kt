package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtScopeGroupConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

// TODO 2.0.4+ refine doc

/**
 * 作用域分组规则。
 *
 * 概述：
 * - 将若干作用域 ID 聚合为命名分组，便于在其它规则中按分组引用与校验（如触发/效应的 `scope_group[...]`）。
 *
 * 路径定位：`scope_groups/{name}`，`{name}` 匹配规则名称（分组名）。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * scope_groups = {
 *     target_species = {
 *         country pop_group leader planet ship fleet army species first_contact
 *     }
 * }
 * ```
 *
 * @property name 名称（分组名）。
 * @property values 分组内的作用域 ID 集合（大小写不敏感）。
 * @property valueConfigMap （计算属性）每个作用域 ID 到其原始值规则的映射。
 */
interface CwtScopeGroupConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty(": string[]")
    val values: Set<@CaseInsensitive String>

    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        /** 由属性规则解析为作用域分组规则。*/
        fun resolve(config: CwtPropertyConfig): CwtScopeGroupConfig?
    }

    companion object : Resolver by CwtScopeGroupConfigResolverImpl()
}
