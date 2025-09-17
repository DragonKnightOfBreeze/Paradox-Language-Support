package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtModifierCategoryConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 修正分类规则。
 *
 * 用于分组修正（modifier），为其指定允许的作用域类型。
 *
 * 路径定位：`modifier_categories/{name}`，`{name}` 匹配规则名称（分类名）。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * modifier_categories = {
 *     Pops = { supported_scopes = { species pop_group planet ... } }
 * }
 * ```
 *
 * @property name 名称（分类名）。
 * @property supportedScopes 允许的作用域（类型）的集合。
 *
 * @see CwtModifierConfig
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.replaceScopes
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.pushScope
 * @see icu.windea.pls.lang.util.ParadoxModifierManager
 */
interface CwtModifierCategoryConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("supported_scopes: string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        /** 由属性规则解析为修正分类规则。*/
        fun resolve(config: CwtPropertyConfig): CwtModifierCategoryConfig?
    }

    companion object : Resolver by CwtModifierCategoryConfigResolverImpl()
}
