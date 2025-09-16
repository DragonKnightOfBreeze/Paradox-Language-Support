package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocationConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 位置规则（location）。
 *
 * 概述：
 * - 描述一条位置键-值（如本地化/图片位置）的要求，并可标注“必需/主要”。
 * - 常用于类型本地化/图片配置等场景中（见 [CwtTypeLocalisationConfig]、[CwtTypeImagesConfig]）。
 *
 * @property key 位置键。
 * @property value 位置值（通常为模板表达式或占位符字符串）。
 * @property required 是否必需。
 * @property primary 是否主要展示项。
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
        /** 由成员属性规则解析为位置规则。*/
        fun resolve(config: CwtPropertyConfig): CwtLocationConfig?
    }

    companion object : Resolver by CwtLocationConfigResolverImpl()
}
