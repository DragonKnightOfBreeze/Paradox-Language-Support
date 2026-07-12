package icu.windea.pls.config.option

import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.config.CwtConfigApiStatus
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtLocationConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.extended.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.extended.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.extended.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.extended.CwtExtendedParameterConfig
import icu.windea.pls.config.config.extended.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.ep.config.config.CwtInjectConfigPostProcessor
import icu.windea.pls.model.ParadoxTagType
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeContext

/**
 * 用于访问选项数据。
 *
 * 说明：
 * - 规则数据指保存在选项注释中的元数据，以 `## ...` 的形式声明。
 * - 仅保存有效的元数据，不支持或者无法识别的元数据会被直接舍弃。
 *
 * 参考：
 * - 规则系统的说明文档：[config.md](https://windea.icu/Paradox-Language-Support/config.md)
 * - 规则格式的参考手册：[ref-config-format.md](https://windea.icu/Paradox-Language-Support/ref-config-format.md)
 *
 * @see CwtMemberConfig
 */
interface CwtOptionDataHolder : UserDataHolder {
    // region Internal

    // NOTE by default, only reserved for internal configs
    val optionConfigs: List<CwtOptionMemberConfig<*>>

    // endregion

    // region Core

    /**
     * API 状态。
     *
     * 部分规则（尤其是可从日志文件生成的规则）需要特殊处理，例如跳过解析，用这个来标记。
     *
     * 适用对象：
     * - 需要特殊处理的规则（目前仅限 [CwtAliasConfig] 和 [CwtSingleAliasConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## api_status = obsolete
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     *
     * @see CwtConfigApiStatus
     */
    val apiStatus: CwtConfigApiStatus?

    /**
     * 允许的出现次数范围（基数表达式）。
     *
     * 指定后续成员规则的匹配次数的上下限。
     * 格式为 `{min}..{max}`，其中 `{min}` 和 `{max}` 分别表示下限和上限。
     * 上下限可以为 `1` 或 `~1` 等形式，上限也可以为 `inf`。
     *
     * 如果未显式指定，且此成员规则的数据类型是常量或枚举值，则推断为 `1..~1`，否则默认使用 `0..inf`。
     *
     * 适用对象：
     * - 定义成员对应的规则。
     *
     * 示例：
     *
     * ```cwt
     * ## cardinality = 0..1
     * ## cardinality = ~1..inf
     * ```
     *
     * > CWTools 兼容性：兼容。
     *
     * @see CwtCardinalityExpression
     */
    val cardinality: CwtCardinalityExpression?

    /**
     * 最小基数，通过指定表达式的定值变量动态指定。
     *
     * 适用对象：
     * - 定义成员对应的规则。
     *
     * 示例：
     *
     * ```cwt
     * ## cardinality_min_define = NGameplay.ETHOS_MIN_POINTS
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val cardinalityMinDefine: String?

    /**
     * 最大基数，通过指定表达式的定值变量动态指定。
     *
     * 适用对象：
     * - 定义成员对应的规则。
     *
     * 示例：
     *
     * ```cwt
     * ## cardinality_max_define = NGameplay.ETHOS_MAX_POINTS
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val cardinalityMaxDefine: String?

    /**
     * 简单结构谓词（结构化过滤）。
     *
     * 使用 `key = value` 或 `key != value` 的形式对匹配进行简单过滤。
     * 值存储为 `ReversibleValue`，其中分隔符 `=` 表示正向、`!=` 表示反向。
     *
     * 适用对象：
     * - 定义成员对应的规则，作为补充过滤条件。
     *
     * 示例：
     *
     * ```cwt
     * ## predicate = { scope = fleet type != country }
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val predicate: Map<String, ReversibleValue<String>>?

    /**
     * 入栈作用域（push scope）。
     *
     * 将指定作用域（通常为 `this`）推入作用域栈，影响后续规则匹配的上下文。
     * 返回值为归一化后的作用域 ID。
     *
     * 适用对象：
     * - 各种可存在作用域上下文的规则（如类型规则、声明规则、别名规则、扩展规则等）。
     *
     * 示例：
     *
     * ```cwt
     * ## push_scope = country
     * ```
     *
     * > CWTools 兼容性：兼容。插件会做作用域 ID 归一化。
     *
     * @see ParadoxScope
     * @see ParadoxScopeContext
     */
    val pushScope: String?

    /**
     * 替换作用域上下文（replace scopes）。
     *
     * 将当前上下文的 `this/root/from/...` 映射到指定的作用域 ID。
     * 支持键名 `replace_scope` 和 `replace_scopes`，二者等价。
     * 值中的作用域会被归一化（大小写和别名）。
     *
     * 适用对象：
     * - 各种可存在作用域上下文的规则（如类型规则、声明规则、别名规则、扩展规则等）。
     *
     * 示例：
     *
     * ```
     * ## replace_scopes = { this = country root = country }
     * ```
     *
     * > CWTools 兼容性：兼容。插件会做作用域 ID 归一化。
     *
     * @see ParadoxScope
     * @see ParadoxScopeContext
     */
    val replaceScopes: Map<String, String>?

    /**
     * 初始的作用域上下文。
     *
     * 来自 [replaceScopes] 和 [pushScope]，经过计算得到。用于后续表达式匹配、补全和校验。
     *
     * 适用对象：
     * - 各种可存在作用域上下文的规则（如类型规则、声明规则、别名规则、扩展规则等）。
     *
     * > CWTools 兼容性：兼容。插件会做作用域 ID 归一化。
     *
     * @see ParadoxScope
     * @see ParadoxScopeContext
     */
    val scopeContext: ParadoxScopeContext?

    /**
     * 允许的作用域（类型）的集合。默认支持任意作用域。
     *
     * 适用对象：
     * - 触发器（trigger）和效果（effect）对应的别名规则（[CwtAliasConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## scope = country
     * ## scopes = { country planet }
     * ```
     *
     * > CWTools 兼容性：兼容。插件会做作用域 ID 归一化。
     *
     * @see ParadoxScope
     * @see ParadoxScopeContext
     */
    val supportedScopes: Set<String>

    /**
     * 类型标识。
     *
     * 用于在定义的扩展规则中声明定义的类型。
     *
     * 适用对象：
     * - 定义的扩展规则（[CwtExtendedDefinitionConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## type = scripted_trigger
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val type: String?

    /**
     * 提示文本。
     *
     * 用于在部分扩展规则中使用，以提供额外的内嵌提示。
     *
     * 适用对象：
     * - 部分扩展规则（如 [CwtExtendedScriptedVariableConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## hint = "Some hint"
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val hint: String?

    /**
     * 事件类型。
     *
     * 用于在动作触发（on action）的扩展规则中指示事件类型（如 `country`、`system`）。
     *
     * 适用对象：
     * - 动作触发（on action）的扩展规则（[CwtExtendedOnActionConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## event_type = country
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val eventType: String?

    /**
     * 上下文键（context key）。
     *
     * 指定参数的上下文，常见形式：`definitionType@definitionName` 或 `inline_script@path`。
     * 插件也支持模板/正则等灵活匹配（详见 [config.md](https://windea.icu/Paradox-Language-Support/config.md)）。
     *
     * 适用对象：
     * - 参数的扩展规则（[CwtExtendedParameterConfig]）。
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val contextKey: String?

    /**
     * 上下文规则的聚合类型。
     *
     * 决定上下文规则是直接来自其属性值规则（`single`），还是来自其中的一组子规则（`multiple`）。
     *
     * 适用对象：
     * - 可指定上下文规则的扩展规则（[CwtExtendedParameterConfig] 和 [CwtExtendedInlineScriptConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## context_configs_type = multiple
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val contextConfigsType: String?

    /**
     * 分组名。
     *
     * 用于对子类型进行分组。
     *
     * 适用对象：
     * - 子类型规则（[CwtSubtypeConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## group = event_type
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val group: String?

    /**
     * 类型键的过滤器（包含/排除，忽略大小写）。
     *
     * 要求类型键必须匹配可选值，可以取反，可以指定多个可选值。
     *
     * 适用对象：
     * - 类型规则（[CwtTypeConfig]）和子类型规则（[CwtSubtypeConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## type_key_filter = country_event
     * ## type_key_filter <> { ship country }
     * ```
     *
     * > CWTools 兼容性：兼容。
     */
    val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?

    /**
     * 类型键的正则过滤器（忽略大小写）。
     *
     * 适用对象：
     * - 类型规则（[CwtTypeConfig]）和子类型规则（[CwtSubtypeConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## type_key_regex = "^ship_.*$"
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val typeKeyRegex: Regex?

    /**
     * 类型键的前缀要求（忽略大小写）。
     *
     * 要求类型键必须以指定前缀开始。
     *
     * 适用对象：
     * - 类型规则（[CwtTypeConfig]）和子类型规则（[CwtSubtypeConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## starts_with = ship_
     * ```
     *
     * > CWTools 兼容性：兼容。
     */
    val startsWith: String?

    /**
     * 排除名单。
     *
     * 指定一个集合，只要“名称不在集合内”即可匹配。
     *
     * 适用对象：
     * - 子类型规则（[CwtSubtypeConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## only_if_not = { simple complex }
     * ```
     *
     * > CWTools 兼容性：兼容。
     */
    val onlyIfNot: Set<String>?

    /**
     * 图相关的关联类型集合（graph related types）。
     *
     * 示例：
     * ```cwt
     * ## graph_related_types = { special_project anomaly_category }
     * ```
     *
     * > CWTools 兼容性：不兼容。插件未使用此类选项。
     */
    val graphRelatedTypes: Set<String>?

    /**
     * 表示当前列声明了一个指定类型的复杂枚举值（而非引用）。
     *
     * 示例：
     *
     * ```cwt
     * ## declare_complex_enum = weapon_tag
     * ```cwt
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val declareComplexEnum: String?

    /**
     * 基于规则的代码检查的严重度（severity）。
     *
     * 示例：
     *
     * ```
     * ## severity = warning
     * ```
     *
     * > CWTools 兼容性：不兼容。插件未使用此类选项。
     */
    val severity: String?

    /**
     * 修正分类键集合（modifier categories）。
     *
     * 脚本化修正分类用到的类别键集合，驱动补全、分组与展示。
     *
     * 示例：
     *
     * ```cwt
     * ## modifier_categories = { economic_unit planet }
     * ```cwt
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val modifierCategories: Set<String>?

    /**
     * 颜色类型（`hex` / `rgb` / `hsv` / `hsv360`）。
     *
     * 指定颜色字段的解析/渲染模式。
     *
     * 适用对象：
     * - 携带了颜色信息的定义成员对应的规则。
     *
     * 示例：
     *
     * ```cwt
     * ## color_type = hex
     * color_hex = scalar
     *
     * ## color_type = rgb
     * color_rgb = {
     *     ## cardinality = 3..3
     *     float
     * }
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val colorType: String?

    /**
     * 允许的文件扩展名集合。
     *
     * 用于约束路径引用，从而提供代码检查与过滤代码补全。
     *
     * 需要注意的是，某些数据类型（如 [Icon][CwtDataTypes.Icon]）与格式（如已指定了扩展名信息的情况）的路径引用不会携带扩展名信息，
     * 因此也不应使用此选项。
     *
     * 适用对象：
     * - 值为路径引用的成员规则（[CwtMemberConfig]）。
     *
     * 示例：
     *
     * ```cwt
     * ## file_extensions = { png dds tga }
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     *
     * @see CwtDataTypeSets.PathReference
     */
    val fileExtensions: Set<String>?

    /**
     * 要注入从而成为当前成员规则的子规则的一组成员规则的路径。
     *
     * 适用对象：
     * - 任意值为子句的成员规则。
     *
     * 示例：
     *
     * ```cwt
     * ## inject = some/file.cwt@some/property
     * ```
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     *
     * @see CwtInjectConfigPostProcessor
     */
    val inject: String?

    // endregion

    // region Flags

    /**
     * 将对应位置的本地化和图片标记为必需项。
     *
     * 适用对象：
     * - 位置规则（[CwtLocationConfig]）。
     *
     * > CWTools 兼容性：兼容。
     */
    val required: Boolean

    /**
     * 将对应位置的本地化和图片标记为主要项。
     * 这意味着它们会作为最相关的本地化和图片，优先显示在快速文档和内嵌提示中。
     *
     * 适用对象：
     * - 位置规则（[CwtLocationConfig]）。
     *
     * > CWTools 兼容性：兼容。
     *
     * @see CwtLocationConfig
     */
    val primary: Boolean

    /**
     * 注明规则上下文和作用域上下文将会被继承。
     * 即，继承自对应的使用处，与其保持一致。
     *
     * 适用对象：
     * - 部分可指定上下文规则的扩展规则（[CwtExtendedParameterConfig]）。
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val inherit: Boolean

    /**
     * 将匹配的单独的值标记为预定义的标签。
     * 脚本文件中的对应的值会启用特殊的语义高亮和文档注释。
     *
     * 适用对象：
     * - 作为单独的值的成员规则（[CwtValueConfig]）。
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     *
     * @see ParadoxTagType
     */
    val tag: Boolean

    /**
     * 将复杂枚举的枚举值标记为忽略大小写。
     *
     * 适用对象：
     * - 复杂枚举规则（[CwtComplexEnumConfig]）。
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val caseInsensitive: Boolean

    /**
     * 将同名同类型的复杂枚举值的等效性限制在定义级别（而非文件级别）。
     *
     * 适用对象：
     * - 复杂枚举规则（[CwtComplexEnumConfig]）。
     *
     * > CWTools 兼容性：不兼容。插件作为扩展提供。
     */
    val perDefinition: Boolean

    // endregion

    fun clear()

    fun copyTo(target: CwtOptionDataHolder)
}
