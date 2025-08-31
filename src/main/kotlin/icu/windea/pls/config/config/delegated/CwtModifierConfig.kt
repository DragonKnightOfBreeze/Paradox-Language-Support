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

/**
 * 修正规则：描述“修正（modifier）”的名称模板、类别与适用作用域。
 *
 * 支持的规则（扩展规则）来源形式：
 * - `type[...]` 下的 `modifiers = { <template_string> = <categories> }`（可按子类型匹配）。
 * - `alias[modifier:xxx] = ...` 中的 `xxx`，需为常量字符串或模板表达式（例如 `mod_<job>_desc`）。
 * - `modifier.cwt` 的 `modifiers = { <template_string> == <categories> }`。
 *
 * 字段：
 * - `name`: 修正名（可能来源于模板展开）。
 * - `categories`: 类别名集合，用于归类与 UI 展示。
 * - `categoryConfigMap`: 类别名到类别规则的映射，便于查询类别的 `supportedScopes` 等信息。
 * - `template`: 名称模板表达式（`CwtTemplateExpression`），在使用处根据上下文（如子类型/占位符）展开。
 * - `supportedScopes`: 该修正在哪些脚本作用域下可用（通常综合自类别的支持范围）。
 */
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
