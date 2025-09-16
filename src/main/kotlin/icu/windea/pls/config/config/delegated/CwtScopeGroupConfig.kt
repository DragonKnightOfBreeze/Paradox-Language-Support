package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtScopeGroupConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 作用域分组规则。
 *
 * 概述：
 * - 将一组作用域 ID 聚合为命名分组，便于在其它规则中按分组引用与校验。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中，读取顶层键 `scope_groups` 下的每个成员属性。
 * - 规则名取自成员属性键，即 `name`（如 `celestial_coordinate`）。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/cwtools-stellaris-config/config/scopes.cwt
 * scope_groups = {
 *     celestial_coordinate = {
 *         planet ship fleet system ambient_object megastructure ...
 *     }
 * }
 * ```
 *
 * @property name 分组名。
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
        /** 由成员属性规则解析为作用域分组规则。*/
        fun resolve(config: CwtPropertyConfig): CwtScopeGroupConfig?
    }

    companion object : Resolver by CwtScopeGroupConfigResolverImpl()
}
