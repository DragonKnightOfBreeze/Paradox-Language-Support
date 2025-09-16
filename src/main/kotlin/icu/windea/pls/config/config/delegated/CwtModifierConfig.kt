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
 * 修正规则（modifier）。
 *
 * 概述：
 * - 统一描述“修正（modifier）”的名称模板、分类与作用域支持，兼容多种 CWT 写法（见下方注释）。
 * - 支持从别名与定义上下文中解析生成，便于在补全、跳转与校验中复用。
 *
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中来自三个来源：
 *   1) 顶层键 `modifiers` 下的成员属性，按键名作为 `name`。
 *   2) 顶层 `alias[modifier:...]`（见 `aliases.cwt`），从别名解析生成，`name` 为 `subName`（可能是模板）。
 *   3) `type[...]{ modifiers { ... } }` 中的条目（在 `CwtTypeConfigResolverImpl` 中处理），以 `typeExpression` 展开 `$` 得到模板化 `name`，并建立 `type2ModifiersMap` 映射。
 *
 * 例：
 * ```cwt
 * # 1) 来自 cwt/cwtools-stellaris-config/config/modifiers.cwt
 * modifiers = {
 *     pop_happiness = { Pops }
 * }
 *
 * # 2) 来自 cwt/cwtools-stellaris-config/config/aliases.cwt
 * alias[modifier:<modifier>] = float
 *
 * # 3) 类型内声明（示意）
 * types = {
 *   type[my_type] = {
 *     modifiers = {
 *       my_type_$ = { Pops }
 *     }
 *   }
 * }
 * ```
 *
 * @property name 修正名称（可能来自模板展开后的常量名）。
 * @property categories 分类名称集合。
 * @property categoryConfigMap 分类名到分类规则的映射。
 * @property template 名称模板表达式（如 `mod_<job>_desc`）。
 * @property supportedScopes 允许出现的作用域集合。
 */
interface CwtModifierConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val categories: Set<String> // category names
    val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig>
    val template: CwtTemplateExpression
    val supportedScopes: Set<String>

    interface Resolver {
        /** 由成员属性规则与覆盖名称 [name] 解析为修正规则。*/
        fun resolve(config: CwtPropertyConfig, name: String): CwtModifierConfig?
        /** 从别名规则解析为修正规则（`alias[modifier:...]`）。*/
        fun resolveFromAlias(config: CwtAliasConfig): CwtModifierConfig
        /** 从定义上下文中的 modifiers 条目解析为修正规则，可指定 [name] 与类型表达式。*/
        fun resolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig?
    }

    companion object : Resolver by CwtModifierConfigResolverImpl()
}
