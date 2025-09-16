package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocalisationPromotionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 本地化提升规则（localisation promotion）。
 *
 * 概述：
 * - 声明某个本地化“提升项”的名称及其允许的作用域集合，供本地化脚本在使用该名称时进行作用域校验与提示。
 * - 由 `localisation_promotion[name] = { ... }` 或相关扩展写法解析而来。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中，读取顶层键 `localisation_promotions` 下的每个成员属性。
 * - 规则名取自成员属性键，即 `name`（如 `Owner`、`Capital`）。
 *
 * 例：
 * ```cwt
 * # 来自 cwt/cwtools-stellaris-config/config/localisation.cwt
 * localisation_promotions = {
 *     Owner = { planet ship starbase system fleet leader army pop_faction ... }
 * }
 * ```
 *
 * @property name 名称。
 * @property supportedScopes 允许的作用域集合。
 */
interface CwtLocalisationPromotionConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromOption(": string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        /** 由成员属性规则解析为本地化提升规则。*/
        fun resolve(config: CwtPropertyConfig): CwtLocalisationPromotionConfig
    }

    companion object : Resolver by CwtLocalisationPromotionConfigResolverImpl()
}
