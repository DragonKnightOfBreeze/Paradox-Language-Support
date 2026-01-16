package icu.windea.pls.config.option

import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.config.CwtApiStatus
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.delegated.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.delegated.CwtExtendedParameterConfig
import icu.windea.pls.config.config.delegated.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.config.delegated.CwtLocationConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.ep.config.config.CwtInjectConfigPostProcessor
import icu.windea.pls.lang.util.ParadoxColorManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.ParadoxTagType
import icu.windea.pls.model.scope.ParadoxScopeContext

/**
 * 用于访问选项数据。
 *
 * 说明：
 * - 规则数据指保存在选项注释中的元数据，以 `## ...` 的形式声明。
 * - 仅保存有效的元数据，不支持或者无法识别的元数据会被直接舍弃。
 *
 * 参考：
 * - CWTools 指引：[references/cwt/guidance.md](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)
 * - PLS 规则系统说明：[config.md](https://windea.icu/Paradox-Language-Support/config.md)
 *
 * @see CwtMemberConfig
 */
interface CwtOptionDataHolder : UserDataHolder {
    fun copyTo(target: CwtOptionDataHolder)

    // region Internal

    // NOTE only reserved for internal configs
    val optionConfigs: List<CwtOptionMemberConfig<*>>

    // endregion

    // region Core

    /**
     * API 状态。
     *
     * 部分规则（尤其是可从日志文件生成的规则）需要特殊处理，例如跳过解析，用这个来标记。
     *
     * 适用对象：需要特殊处理的规则（目前支持 [CwtSingleAliasConfig] 和 [CwtAliasConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## api_status = obsolete`
     */
    val apiStatus: CwtApiStatus?

    /**
     * 允许的出现次数范围（基数表达式）。
     *
     * 指定后续规则项的匹配次数的上下限，形如 `min..max`，其中 `max` 可为 `inf` 或 `~1` 等形式。
     * 如果未显式指定，且该定义成员为常量类型，则默认推断为 `1..~1`（必须出现一次，超过仅作弱警告）。
     *
     * 适用对象：定义成员对应的规则。
     *
     * CWTools 兼容性：兼容。
     *
     * 示例：`## cardinality = 0..1`
     */
    val cardinality: CwtCardinalityExpression?

    /**
     * 最小基数，从定值（define）动态获取。
     *
     * 通过字符串路径（如 `NGameplay/ETHOS_MIN_POINTS`）动态指定最小次数。
     *
     * 适用对象：定义成员对应的规则。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## cardinality_min_define = "NGameplay/ETHOS_MIN_POINTS"`
     */
    val cardinalityMinDefine: String?

    /**
     * 最大基数，从定值（define）动态获取。
     *
     * 适用对象：定义成员对应的规则。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## cardinality_max_define = "NGameplay/ETHOS_MAX_POINTS"`
     */
    val cardinalityMaxDefine: String?

    /**
     * 简单结构谓词（结构化过滤）。
     *
     * 使用 `key = value` 或 `key != value` 的形式对匹配进行简单过滤。
     * 值存储为 `ReversibleValue`，其中分隔符 `=` 表示正向、`!=` 表示反向。
     *
     * 适用对象：定义成员对应的规则，作为补充过滤条件。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## predicate = { scope = fleet type != country }`
     */
    val predicate: Map<String, ReversibleValue<String>>?

    /**
     * 替换作用域上下文（replace scopes）。
     *
     * 将当前上下文的 `this/root/from/...` 映射到指定的作用域 ID。
     * 支持键名 `replace_scope` 和 `replace_scopes`，二者等价。
     * 值中的作用域会被 PLS 归一化（大小写和别名）。
     *
     * 适用对象：各种可存在作用域上下文的规则（如定义、别名、扩展规则等）。
     *
     * CWTools 兼容性：兼容。PLS 会做作用域 ID 归一化。
     *
     * 示例：`## replace_scopes = { this = country root = country }`
     *
     * @see ParadoxScopeManager
     */
    val replaceScopes: Map<String, String>?

    /**
     * 入栈作用域（push scope）。
     *
     * 将指定作用域（通常为 `this`）推入作用域栈，影响后续规则匹配的上下文。
     * 返回值为归一化后的作用域 ID。
     *
     * 适用对象：各种可存在作用域上下文的规则（如定义、别名、扩展规则等）。
     *
     * CWTools 兼容性：兼容。PLS 会做作用域 ID 归一化。
     *
     * 示例：`## push_scope = country`
     *
     * @see ParadoxScopeManager
     */
    val pushScope: String?

    /**
     * 初始的作用域上下文。
     *
     * 来自 [replaceScopes] 和 [pushScope]，经过计算得到。用于后续表达式匹配、补全和校验。
     *
     * 适用对象：各种可存在作用域上下文的规则（如定义、别名、扩展规则等）。
     *
     * CWTools 兼容性：兼容。PLS 会做作用域 ID 归一化。
     *
     * @see ParadoxScopeManager
     */
    val scopeContext: ParadoxScopeContext?

    /**
     * 允许的作用域（类型）的集合。默认支持任意作用域。
     *
     * 适用对象：触发器（trigger）和效果（effect）对应的别名规则（[CwtAliasConfig]）。
     *
     * CWTools 兼容性：兼容。PLS 会做作用域 ID 归一化。
     *
     * 示例：
     * ```cwt
     * ## scope = country
     * ## scope = { country planet }
     * ```
     *
     * @see ParadoxScopeManager
     */
    val supportedScopes: Set<String>

    /**
     * 类型标识。
     *
     * 用于在定义的扩展规则中声明定义的类型。
     *
     * 适用对象：定义的扩展规则（[CwtExtendedDefinitionConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## type = scripted_trigger`
     */
    val type: String?

    /**
     * 提示信息。
     *
     * 用于在部分扩展规则中使用，以提供额外的内嵌提示。
     *
     * 适用对象：部分扩展规则（如 [CwtExtendedScriptedVariableConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## hint = "一些提示"`
     */
    val hint: String?

    /**
     * 事件类型。
     *
     * 用于在 on action 的扩展规则中指示事件类型（如 `country`、`system`）。
     *
     * 适用对象：on action 的扩展规则（[CwtExtendedOnActionConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## event_type = country`
     */
    val eventType: String?

    /**
     * 上下文键（context key）。
     *
     * 指定参数的上下文，常见形式：`definitionType@definitionName` 或 `inline_script@path`。
     * PLS 也支持模板/正则等灵活匹配（详见 [config.md](https://windea.icu/Paradox-Language-Support/config.md)）。
     *
     * 适用对象：参数的扩展规则（[CwtExtendedParameterConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     */
    val contextKey: String?

    /**
     * 上下文配置的聚合类型。
     *
     * 指定 `x = {...}` 根下的“上下文配置”是单个（`single`）还是多个（`multiple`）。
     * 默认为 `single`。
     *
     * 适用对象：可指定规则上下文的扩展规则（[CwtExtendedInlineScriptConfig] 和 [CwtExtendedParameterConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## context_configs_type = multiple`
     */
    val contextConfigsType: String

    /**
     * 分组名。
     *
     * 用于对子类型进行分组。
     *
     * 适用对象：子类型规则（[CwtSubtypeConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## group = ships`
     */
    val group: String?

    /**
     * 类型键的过滤器（包含/排除，忽略大小写）。
     *
     * 要求类型键必须匹配可选值，可以取反，可以指定多个可选值。
     *
     * 适用对象：类型规则（[CwtTypeConfig]）和子类型规则（[CwtSubtypeConfig]）。
     *
     * CWTools 兼容性：兼容。
     *
     * 示例：
     * ```cwt
     * ## type_key_filter = country_event
     * ## type_key_filter <> { ship country }
     * ```
     */
    val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?

    /**
     * 类型键的正则过滤器（忽略大小写）。
     *
     * 适用对象：类型规则（[CwtTypeConfig]）和子类型规则（[CwtSubtypeConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## type_key_regex = "^ship_.*$"`
     */
    val typeKeyRegex: Regex?

    /**
     * 类型键的前缀要求（忽略大小写）。
     *
     * 要求类型键必须以指定前缀开始。
     *
     * 适用对象：类型规则（[CwtTypeConfig]）和子类型规则（[CwtSubtypeConfig]）。
     *
     * CWTools 兼容性：兼容。
     *
     * 示例：`## starts_with = ship_`
     */
    val startsWith: String?

    /**
     * 排除名单（only_if_not）。
     *
     * 指定一个集合，只要“名称不在集合内”即可匹配。
     *
     * 适用对象：子类型规则（[CwtSubtypeConfig]）。
     *
     * CWTools 兼容性：兼容。
     *
     * 示例：`## only_if_not = { simple complex }`
     */
    val onlyIfNot: Set<String>?

    /**
     * 图相关的关联类型集合（graph_related_types）。
     *
     * 示例：`## graph_related_types = { special_project anomaly_category }`
     *
     * CWTools 兼容性：PLS 目前未使用这类选项数据。
     */
    val graphRelatedTypes: Set<String>?

    /**
     * （基于规则的代码检查的）严重度。
     *
     * 示例：`## severity = warning`
     *
     * CWTools 兼容性：PLS 目前未使用这类选项数据。
     */
    val severity: String?

    /**
     * 允许的文件扩展名集合（file_extensions）。
     *
     * 用于约束路径引用，从而提供代码检查与过滤代码补全。
     *
     * 适用对象：值为路径引用的成员规则（[CwtMemberConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## file_extensions = { png dds tga }`
     *
     * @see CwtDataTypeGroups.PathReference
     */
    val fileExtensions: Set<String>?

    /**
     * 修正分类键集合（modifier_categories）。
     *
     * 脚本化修正分类用到的类别键集合，驱动补全、分组与展示。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## modifier_categories = { economic_unit planet }`
     *
     * @see ParadoxModifierManager
     */
    val modifierCategories: Set<String>?

    /**
     * 颜色类型（`hex` / `rgb` / `hsv` / `hsv360`）。
     *
     * 指定颜色值的解析/渲染模式。
     *
     * 适用对象：携带了颜色信息的定义成员对应的规则。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## color_type = rgb`
     *
     * @see ParadoxColorManager
     */
    val colorType: String?

    /**
     * 要注入从而成为当前成员规则的子规则的一组成员规则的路径。
     *
     * 适用对象：任意值为子句的成员规则。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## inject = some/file.cwt@some/property`
     *
     * @see CwtInjectConfigPostProcessor
     */
    val inject: String?

    // endregion

    // region Flags

    /**
     * 将对应位置的本地化和图片标记为必需项。
     *
     * 适用对象：位置规则（[CwtLocationConfig]）。
     *
     * CWTools 兼容性：兼容。
     */
    val required: Boolean

    /**
     * 将对应位置的本地化和图片标记为主要项。
     * 这意味着它们会作为最相关的本地化和图片，优先显示在快速文档和内嵌提示中。
     *
     * 适用对象：位置规则（[CwtLocationConfig]）。
     *
     * CWTools 兼容性：兼容。
     *
     * @see CwtLocationConfig
     */
    val primary: Boolean

    /**
     * 注明规则上下文和作用域上下文将会被继承。
     * 即，继承自对应的使用处，与其保持一致。
     *
     * 适用对象：部分可指定规则上下文的扩展规则（如 [CwtExtendedParameterConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     */
    val inherit: Boolean

    /**
     * 将匹配的单独的值标记为预定义的标签。
     * 脚本文件中的对应的值会启用特殊的语义高亮和文档注释。
     *
     * 适用对象：作为单独的值的成员规则（[CwtValueConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * @see ParadoxTagType
     */
    val tag: Boolean

    /**
     * 将复杂枚举的枚举值标记为忽略大小写。
     *
     * 适用对象：复杂枚举规则（[CwtComplexEnumConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     */
    val caseInsensitive: Boolean

    /**
     * 将同名同类型的复杂枚举值的等效性限制在定义级别（而非文件级别）。
     *
     * 适用对象：复杂枚举规则（[CwtComplexEnumConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     */
    val perDefinition: Boolean

    // endregion
}
