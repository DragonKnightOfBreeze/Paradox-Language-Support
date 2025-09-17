package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocationConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 位置规则。
 *
 * 用于定位目标资源（图片、本地化等）的来源。
 * 具体而言，通过位置表达式（[CwtLocationExpression]）进行定位。
 *
 * 路径定位：
 * 1. 本地化资源：`types/type[{type}]/localisation/{key}`，`{type}` 匹配定义类型，`{key}` 匹配键名。
 * 2. 图片资源：`types/type[{type}]/images/{key}`，`{type}` 匹配定义类型，`{key}` 匹配键名。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * types = {
 *     type[army] = {
 *         # ...
 *         images = {
 *             ## primary
 *             icon = icon # <sprite>
 *         }
 *     }
 * }
 * ```
 *
 * @property key 资源的名字。
 * @property value 资源的位置表达式（[CwtLocationExpression]）。
 * @property required 是否是必需项。
 * @property primary 是否是主要项。
 *
 * @see CwtTypeImagesConfig
 * @see CwtTypeLocalisationConfig
 * @see CwtLocationExpression
 */
interface CwtLocationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val key: String
    @FromProperty(": string")
    val value: String
    @FromOption("required")
    val required: Boolean
    @FromOption("primary")
    val primary: Boolean

    interface Resolver {
        /** 由属性规则解析为位置规则。*/
        fun resolve(config: CwtPropertyConfig): CwtLocationConfig?
    }

    companion object : Resolver by CwtLocationConfigResolverImpl()
}
