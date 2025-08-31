package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtModifierConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.cwt.psi.CwtProperty

// CWT规则文件中关于（生成的）修正的规则有多种写法
// * 写在 type[xxx] = {...} 子句中的 modifiers = { ... } 子句中，允许按子类型进行匹配，格式为 <template_string> = <categories>
// * 作为 alias[modifier:xxx] = x 中的 xxx ，这里PLS认为 xxx 需要表示一个常量字符串或者模版表达式（如 mod_<job>_desc）
// * 写在 modifier.cwt 中的 modifiers = { ... } 子句中，格式为 <template_string> == <categories>
// 目前PLS支持以上所有三种写法

interface CwtModifierConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val categories: Set<String> // category names
    val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig>
    val template: CwtTemplateExpression
    val supportedScopes: Set<String>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig, name: String): CwtModifierConfig?
        fun resolveFromAlias(config: CwtAliasConfig): CwtModifierConfig
        fun resolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig?
    }

    companion object : Resolver by CwtModifierConfigResolverImpl()
}
