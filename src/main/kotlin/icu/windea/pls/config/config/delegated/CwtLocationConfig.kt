package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocationConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 位置规则：描述某一键对应的资源/文本位置表达式。
 *
 * - 常与 `CwtTypeLocalisationConfig`/`CwtTypeImagesConfig` 搭配，分别用于定义“本地化位置”和“图像位置”。
 * - 典型键如 `name`、`title`、`desc`（本地化），以及 `icon`（图像）等。
 *
 * 字段：
 * - `key`: 位置键名。
 * - `value`: 位置表达式字符串（由上层解析为具体资源/文本）。
 * - `required`: 是否为必需位置。
 * - `primary`: 是否为主要位置（在多候选时优先使用）。
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
        fun resolve(config: CwtPropertyConfig): CwtLocationConfig?
    }

    companion object : Resolver by CwtLocationConfigResolverImpl()
}
