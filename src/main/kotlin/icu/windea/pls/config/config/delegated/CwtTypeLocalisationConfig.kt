package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtTypeLocalisationConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 类型本地化规则。
 *
 * 用于定位对应类型的定义的相关本地化，以便在 UI 与各种提示信息中展示。
 * 具体而言，通过位置表达式（[CwtLocationExpression]）进行定位，并最终解析为本地化。
 *
 * 路径定位：`types/type[{type}]/localisation`，`{type}` 匹配定义类型。
 *
 * CWTools 兼容性：兼容，但存在一定的扩展。
 *
 * 示例：
 * ```cwt
 * types = {
 *     type[ship_design] = {
 *         # ...
 *         localisation = {
 *             ## primary
 *             name = some_loc_key
 *             subtype[corvette] = {
 *                 name = some_corvette_loc_key
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @property locationConfigs 子类型表达式与位置规则的配对列表。
 *
 * @see CwtLocationExpression
 */
interface CwtTypeLocalisationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> // (subtypeExpression, locationConfig)

    /** 按给定的 [subtypes] 合并与筛选后，返回生效的本地化位置规则列表。*/
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>

    interface Resolver {
        /** 由属性规则解析为类型本地化规则。*/
        fun resolve(config: CwtPropertyConfig): CwtTypeLocalisationConfig?
    }

    companion object : Resolver by CwtTypeLocalisationConfigResolverImpl()
}
