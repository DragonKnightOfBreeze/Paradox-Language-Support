package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtTypeImagesConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 类型图片规则。
 *
 * 用于定位对应类型的定义的相关图片，以便在 UI 与各种提示信息中展示。
 * 具体而言，通过位置表达式（[CwtLocationExpression]）进行定位，并最终解析为处理后的图片。
 *
 * 路径定位：`types/type[{type}]/images`，`{type}` 匹配定义类型。
 *
 * CWTools 兼容性：兼容，但存在一定的扩展。
 *
 * 示例：
 * ```cwt
 * types = {
 *     type[component_template] = {
 *         # ...
 *         images = {
 *             ## primary
 *             ## required
 *             icon = "icon|icon_frame"
 *         }
 *     }
 * }
 * ```
 *
 * @property locationConfigs 子类型表达式与位置规则的配对列表。
 *
 * @see CwtLocationExpression
 */
interface CwtTypeImagesConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> // (subtypeExpression, locationConfig)

    /** 按给定的 [subtypes] 合并与筛选后，返回生效的图片位置规则列表。*/
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>

    interface Resolver {
        /** 由属性规则解析为类型图片规则。*/
        fun resolve(config: CwtPropertyConfig): CwtTypeImagesConfig?
    }

    companion object : Resolver by CwtTypeImagesConfigResolverImpl()
}
