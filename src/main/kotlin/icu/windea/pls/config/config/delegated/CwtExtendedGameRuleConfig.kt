package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtExtendedGameRuleConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtMemberElement

/**
 * 游戏规则的扩展规则。
 *
 * 用于为对应的游戏规则（game rule）提供额外的提示信息（如文档注释、内嵌提示），以及重载声明规则。
 *
 * 说明：
 * - 规则名称可以是常量、模版表达式、ANT 表达式或正则（见 [CwtDataTypeGroups.PatternAware]）。
 * - 游戏规则（game rule）即类型为 `game_rule` 的定义。
 *
 * 路径定位：`game_rules/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：不兼容，拥有不同的格式与行为。
 *
 * 示例：
 * ```cwt
 * game_rules = {
 *     ### Some documentation
 *     ## hint = §RSome hint text§!
 *     x # or `x = xxx` to override declaration config
 * }
 * ```
 *
 * @property name 名称。
 * @property hint 额外提示信息（可选）。
 * @property configForDeclaration 可直接用于检查定义声明的结构，经过处理后的属性规则（如果重载了声明规则）。
 */
interface CwtExtendedGameRuleConfig : CwtDelegatedConfig<CwtMemberElement, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("hint: string?")
    val hint: String?

    val configForDeclaration: CwtPropertyConfig?

    interface Resolver {
        /** 由成员规则解析为游戏规则的扩展规则。*/
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedGameRuleConfig
    }

    companion object : Resolver by CwtExtendedGameRuleConfigResolverImpl()
}
