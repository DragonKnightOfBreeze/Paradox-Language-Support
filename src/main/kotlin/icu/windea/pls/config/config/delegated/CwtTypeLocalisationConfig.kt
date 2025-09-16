package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtTypeLocalisationConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 类型本地化规则。
 *
 * 概述：
 * - 为某个“定义类型”的不同子类型声明对应的本地化位置配置列表，便于在 UI 与文档中展示。
 * - 由 `type_localisation[...]`（或等效扩展）中的条目解析而来。
 *
 * @property locationConfigs 子类型表达式与位置规则的配对列表（`Pair<subtypeExpression?, CwtLocationConfig>`）。
 *
 * 定位：
 * - 在 `CwtTypeConfigResolverImpl` 中，当处理 `type[...]` 的成员属性键为 `localisation` 时，解析为本规则。
 * - `locationConfigs` 来自 `localisation = { ... }` 的成员属性与可选的 `subtype[...]` 分节。
 *
 * 例：
 * ```cwt
 * types = {
 *   type[ship_design] = {
 *     localisation = {
 *       ## primary
 *       name = some_loc_key
 *       subtype[corvette] = { name = some_corvette_loc_key }
 *     }
 *   }
 * }
 * ```
 */
interface CwtTypeLocalisationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> // (subtypeExpression, locationConfig)

    /** 按给定的 [subtypes] 合并与筛选后，返回生效的本地化位置规则列表。*/
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>

    interface Resolver {
        /** 由成员属性规则解析为类型本地化规则。*/
        fun resolve(config: CwtPropertyConfig): CwtTypeLocalisationConfig?
    }

    companion object : Resolver by CwtTypeLocalisationConfigResolverImpl()
}
