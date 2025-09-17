package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtScopeGroupConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.ParadoxScope
import icu.windea.pls.model.ParadoxScopeContext

/**
 * 作用域分组规则。
 *
 * 用于分组作用域类型（scope type），便于在其他规则中按分组引用与校验。
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
 * @property valueConfigMap 每个作用域 ID 到其原始值规则的映射。
 *
 * @see ParadoxScope
 * @see ParadoxScopeContext
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
