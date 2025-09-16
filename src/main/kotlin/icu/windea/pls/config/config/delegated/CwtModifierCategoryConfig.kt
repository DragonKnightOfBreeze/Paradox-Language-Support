package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtModifierCategoryConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 修正分类规则（modifier category）。
 *
 * 概述：
 * - 声明某个“修正分类”以及该分类允许出现的作用域集合。
 * - 常与修正规则（见 [CwtModifierConfig]）配合使用，用于校验与提示。
 *
 * @property name 分类名。
 * @property supportedScopes 允许的作用域集合。
 */
interface CwtModifierCategoryConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("supported_scopes: string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        /** 由成员属性规则解析为修正分类规则。*/
        fun resolve(config: CwtPropertyConfig): CwtModifierCategoryConfig?
    }

    companion object : Resolver by CwtModifierCategoryConfigResolverImpl()
}
