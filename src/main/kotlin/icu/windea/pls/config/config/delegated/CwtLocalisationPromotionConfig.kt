package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocalisationPromotionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 本地化晋升规则：描述在本地化文本中可用的“晋升（promotion）”标签及其可接受的输入作用域。
 *
 * - 示例来源：`cwtools-stellaris-config/config/localisation.cwt` 的 `localisation_promotions`。
 * - 语义：定义名称（如 `Owner`、`Planet` 等）与一组允许的脚本作用域，用于在本地化上下文中判定该晋升是否可用。
 *
 * 字段：
 * - `name`: 晋升名。
 * - `supportedScopes`: 允许作为输入的脚本作用域集合。
 */
interface CwtLocalisationPromotionConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromOption(": string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLocalisationPromotionConfig
    }

    companion object : Resolver by CwtLocalisationPromotionConfigResolverImpl()
}
