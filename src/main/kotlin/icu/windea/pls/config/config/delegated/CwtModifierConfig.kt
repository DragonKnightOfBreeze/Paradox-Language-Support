package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.CwtDataTypes.Constant
import icu.windea.pls.config.CwtDataTypes.TemplateExpression
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtModifierConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 修正规则。
 *
 * 用于提供修正（modifier）的相关信息（快速文档、命名、分类）。
 *
 * **修正（modifier）**是脚本中一种常见的**谓语**，用于调整游戏数值。
 * 其名字是预定义的，也可能是动态生成的。
 * 其允许的作用域类型则来自所属的分类。
 * 可参见：`modifiers.log`。
 *
 * 规则名称在这里用于匹配（而非等同于）修正名，可以是：
 * - 常量（[Constant]） - 匹配预定义的修正。
 * - 模版表达式（[TemplateExpression]） - 匹配动态生成的修正。
 *
 * 路径定位：
 * 1. `modifiers/{name}`，`{name}` 匹配规则名称。
 * 2. `types/type[{type}]/modifiers/{name}`，`{type}` 匹配定义类型，`{name}`匹配规则名称（其中的 `$` 会被替换为 `<{type}>`）。
 * 3. `types/type[{type}]/modifiers/subtype[{subtype}]/{name}`，`{subtype}` 匹配定义的子类型。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * # 来自 modifiers.cwt
 * modifiers = {
 *     pop_happiness = { Pops } # `Pops` is the modifier category
 *     job_<job>_add = { Planets } # for generated modifiers
 *     # ...
 * }
 *
 * # 在类型规则中声明
 * types = {
 *     type[job] = {
 *         modifiers = {
 *             job_$_add = { Planets } # will be resolved to `job_<job>_add`
 *         }
 *         # ...
 *     }
 * }
 * ```
 *
 * @property name 名称。
 * @property categories 分类名的集合。
 * @property categoryConfigMap 分类名到分类规则的映射。
 * @property template 名称对应的模板表达式（如 `job_<job>_add`）。
 * @property supportedScopes 允许的作用域（类型）的集合。
 *
 * @see CwtModifierCategoryConfig
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.replaceScopes
 * @see icu.windea.pls.config.util.data.CwtOptionDataAccessors.pushScope
 * @see icu.windea.pls.lang.util.ParadoxModifierManager
 */
interface CwtModifierConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val categories: Set<String> // category names
    val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig>
    val template: CwtTemplateExpression
    val supportedScopes: Set<String>

    interface Resolver {
        /** 由属性规则解析为修正规则。*/
        fun resolve(config: CwtPropertyConfig, name: String): CwtModifierConfig?
        /** 由别名规则（`alias[modifier:...] = ...`）解析为修正规则。*/
        fun resolveFromAlias(config: CwtAliasConfig): CwtModifierConfig
        /** 从定义上下文中的 modifiers 条目解析为修正规则，可指定 [name] 与类型表达式。*/
        fun resolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig?
    }

    companion object : Resolver by CwtModifierConfigResolverImpl()
}
