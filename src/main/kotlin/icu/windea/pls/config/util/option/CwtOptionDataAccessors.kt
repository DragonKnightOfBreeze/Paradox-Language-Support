package icu.windea.pls.config.util.option

import icu.windea.pls.config.CwtApiStatus
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtOptionConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.delegated.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.delegated.CwtExtendedParameterConfig
import icu.windea.pls.config.config.delegated.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.config.util.option.CwtOptionDataAccessors.pushScope
import icu.windea.pls.config.util.option.CwtOptionDataAccessors.replaceScopes
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.FastMap
import icu.windea.pls.core.collections.FastSet
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.scope.ParadoxScopeContext

/**
 * 提供“选项数据访问器”（Option Data Accessors）的集中定义与实现。
 *
 * 用途与行为（重要）：
 * - 这些 accessor 以统一、类型安全、可带缓存的方式，从某个成员规则（`CwtMemberConfig`）附带的选项（`CwtOptionMemberConfig`）里提取所需的数据。
 * - 统一的调用方式为：`config.optionData { someAccessor }`。
 * - 一些 accessor 内建缓存（`create(cached = true)`），避免在解析、补全等流程中重复解析选项。
 * - 对于作用域相关的选项（如 `## replace_scopes`、`## push_scope`、`## scopes`），PLS 会做大小写与别名归一化处理（参见 `ParadoxScopeManager`）。
 * - 对于部分选项，CWTools 原版不支持或语义不同，其兼容性会在各自的 KDoc 中明确说明。
 *
 * 参考：
 * - CWTools 指引：[references/cwt/guidance.md](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md)
 * - PLS 规则系统说明：[config.md](https://windea.icu/Paradox-Language-Support/config.md)
 *
 * @see CwtOptionDataAccessor
 */
@Optimized
object CwtOptionDataAccessors : CwtOptionDataAccessorMixin {
    fun <T> create(cached: Boolean = false, action: CwtMemberConfig<*>.() -> T): CwtOptionDataAccessorProvider<T> {
        return CwtOptionDataAccessorProvider(cached, action)
    }

    /**
     * 选项标志。
     *
     * 成员规则上可以存在多个单独且类似标识符的选项值，用于附加布尔型标志。
     */
    val flags: CwtOptionDataAccessor<CwtOptionFlags> by create {
        CwtOptionFlags.from(this)
    }

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
    val apiStatus: CwtOptionDataAccessor<CwtApiStatus?> by create {
        findOption("api_status")?.stringValue?.let { CwtApiStatus.get(it) }
    }

    /**
     * 允许的出现次数范围（基数表达式）。
     *
     * 指定后续规则项的匹配次数上下限，形如 `min..max`，其中 `max` 可为 `inf` 或 `~1` 等形式。
     * 若未显式指定且该成员为“常量类型”，PLS 默认推断为 `1..~1`（必须出现一次，超过仅作弱警告）。
     *
     * 适用对象：定义成员对应的规则。
     *
     * CWTools 兼容性：兼容。默认推断逻辑为 PLS 的增强（详见 [config.md](https://windea.icu/Paradox-Language-Support/config.md)）。
     *
     * 示例：`## cardinality = 0..1`
     */
    val cardinality: CwtOptionDataAccessor<CwtCardinalityExpression?> by create(cached = true) {
        val option = findOption("cardinality")
        if (option == null) {
            // 如果没有注明且类型是常量或枚举值，则推断为 1..~1
            val dataType = configExpression.type
            if (dataType == CwtDataTypes.Constant || dataType == CwtDataTypes.EnumValue) {
                return@create CwtCardinalityExpression.resolve("1..~1")
            }
        }
        option?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
    }

    /**
     * 最小基数（从 Define 预设动态获取）。
     *
     * 通过字符串路径（如 `NGameplay/ETHOS_MIN_POINTS`）动态指定最小次数。
     *
     * 适用对象：定义成员对应的规则。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## cardinality_min_define = "NGameplay/ETHOS_MIN_POINTS"`
     */
    val cardinalityMinDefine: CwtOptionDataAccessor<String?> by create {
        findOption("cardinality_min_define")?.stringValue
    }

    /**
     * 最大基数（从 Define 预设动态获取）。
     *
     * 适用对象：定义成员对应的规则。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## cardinality_max_define = "NGameplay/ETHOS_MAX_POINTS"`
     */
    val cardinalityMaxDefine: CwtOptionDataAccessor<String?> by create {
        findOption("cardinality_max_define")?.stringValue
    }

    /**
     * 简单结构谓词（结构化过滤）。
     *
     * 使用 `key = value` 或 `key != value` 的形式对匹配进行简单过滤。
     * 值存储为 `ReversibleValue`，其中分隔符 `=` 表示正向、`!=` 表示反向。
     *
     * 适用对象：定义成员对应的规则，作为补充过滤条件。
     *
     * CWTools 兼容性：PLS 扩展。用于特定的过滤逻辑。
     *
     * 示例：`## predicate = { scope = fleet type != country }`
     */
    val predicate: CwtOptionDataAccessor<Map<String, ReversibleValue<String>>?> by create(cached = true) {
        val option = findOption("predicate") ?: return@create null
        val optionConfigs = option.optionConfigs ?: return@create null
        if (optionConfigs.isEmpty()) return@create emptyMap()
        val r = FastMap<String, ReversibleValue<String>>()
        optionConfigs.forEachFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionConfig) return@f
            val k = optionConfig.key
            val v = ReversibleValue(optionConfig.separatorType == CwtSeparatorType.EQUAL, optionConfig.value)
            r[k] = v
        }
        r.optimized()
    }

    /**
     * 替换作用域上下文（replace scopes）。
     *
     * 将当前上下文的 `this/root/from/...` 映射到指定的作用域 ID。
     * 支持键名 `replace_scope` 与 `replace_scopes`，二者等价。
     * 值中的作用域会被 PLS 归一化（大小写与别名）。
     *
     * 适用对象：各种可存在作用域上下文的规则（如定义、别名、扩展规则等）。
     *
     * CWTools 兼容性：兼容。PLS 会做作用域 ID 归一化。
     *
     * 示例：`## replace_scopes = { this = country root = country }`
     *
     * @see ParadoxScopeManager
     */
    val replaceScopes: CwtOptionDataAccessor<Map<String, String>?> by create(cached = true) {
        val option = findOption("replace_scope", "replace_scopes") ?: return@create null
        val optionConfigs = option.optionConfigs ?: return@create null
        if (optionConfigs.isEmpty()) return@create emptyMap()
        val r = FastMap<String, String>()
        optionConfigs.forEachFast f@{ optionConfig ->
            if (optionConfig !is CwtOptionConfig) return@f
            // ignore case for both system scopes and scopes (to lowercase)
            val k = optionConfig.key.lowercase()
            val v = optionConfig.stringValue?.let { ParadoxScopeManager.getScopeId(it) } ?: return@f
            r[k] = v
        }
        r.optimized()
    }

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
    val pushScope: CwtOptionDataAccessor<String?> by create(cached = true) {
        val option = findOption("push_scope")
        option?.getOptionValue()?.let { v -> ParadoxScopeManager.getScopeId(v) }
    }

    /**
     * 初始的作用域上下文。
     *
     * 来自 [replaceScopes] 与 [pushScope]，经过计算得到。用于后续表达式匹配、补全与校验。
     *
     * 适用对象：各种可存在作用域上下文的规则（如定义、别名、扩展规则等）。
     *
     * CWTools 兼容性：兼容。PLS 会做作用域 ID 归一化。
     *
     * @see ParadoxScopeManager
     */
    val scopeContext: CwtOptionDataAccessor<ParadoxScopeContext?> by create(cached = true) {
        val replaceScopes = optionData { replaceScopes }
        val pushScope = optionData { pushScope }
        val scopeContext = replaceScopes?.let { ParadoxScopeContext.get(it) }
        scopeContext?.resolveNext(pushScope) ?: pushScope?.let { ParadoxScopeContext.get(it, it) }
    }

    /**
     * 支持的作用域。默认支持任意作用域。
     *
     * 适用对象：触发器（trigger）与效果（effect）对应的别名规则（[CwtAliasConfig]）。
     *
     * CWTools 兼容性：兼容。PLS 会做作用域 ID 归一化。
     *
     * 示例：
     * ```cwt
     * ## scope = country
     * ## scope = { country planet }
     * ```
     */
    val supportedScopes: CwtOptionDataAccessor<Set<String>> by create(cached = true) {
        // ignore case for scopes (to lowercase)
        val option = findOption("scope", "scopes")
        val r = option?.getOptionValueOrValues()?.mapTo(FastSet()) { ParadoxScopeManager.getScopeId(it) }
        if (r.isNullOrEmpty()) ParadoxScopeManager.anyScopeIdSet else r.optimized()
    }

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
    val type: CwtOptionDataAccessor<String?> by create {
        findOption("type")?.stringValue
    }

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
    val hint: CwtOptionDataAccessor<String?> by create {
        findOption("hint")?.stringValue
    }

    /**
     * 事件类型。
     *
     * 用于在 on action 的扩展规则中指示事件类型（如 `country`、`system`）。
     *
     * 适用对象：on action 的扩展规则（[CwtExtendedOnActionConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。用于 on_action 相关的解析与显示。
     *
     * 示例：`## event_type = country`
     */
    val eventType: CwtOptionDataAccessor<String?> by create {
        findOption("event_type")?.stringValue
    }

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
    val contextKey: CwtOptionDataAccessor<String?> by create {
        findOption("context_key")?.stringValue
    }

    /**
     * 上下文配置的聚合类型。
     *
     * 指定 `x = {...}` 根下的“上下文配置”是单个（`single`）还是多个（`multiple`）。
     * 未指定时默认 `single`。
     *
     * 适用对象：可指定规则上下文的扩展规则（[CwtExtendedInlineScriptConfig] 和 [CwtExtendedParameterConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。见 `inline_scripts`、`parameters` 章节。
     *
     * 示例：`## context_configs_type = multiple`
     */
    val contextConfigsType: CwtOptionDataAccessor<String> by create {
        findOption("context_configs_type")?.stringValue ?: "single"
    }

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
    val group: CwtOptionDataAccessor<String?> by create {
        findOption("group")?.stringValue
    }

    /**
     * 类型键的过滤器（包含/排除，忽略大小写）。
     *
     * 要求类型键必须匹配可选值，可以取反，可以指定多个可选值。
     *
     * 适用对象：类型规则（[CwtTypeConfig]）与子类型规则（[CwtSubtypeConfig]）。
     *
     * CWTools 兼容性：兼容。
     *
     * 示例：
     * ```cwt
     * ## type_key_filter = country_event
     * ## type_key_filter <> { ship country }
     * ```
     */
    val typeKeyFilter: CwtOptionDataAccessor<ReversibleValue<Set<@CaseInsensitive String>>?> by create {
        // 值可能是 string 也可能是 stringArray
        val option = findOption("type_key_filter") ?: return@create null
        val values = option.getOptionValueOrValues() ?: return@create null
        val set = caseInsensitiveStringSet().apply { addAll(values) } // 忽略大小写
        val positive = option.separatorType == CwtSeparatorType.EQUAL
        ReversibleValue(positive, set.optimized())
    }

    /**
     * 类型键的正则过滤器（忽略大小写）。
     *
     * 适用对象：类型规则（[CwtTypeConfig]）与子类型规则（[CwtSubtypeConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## type_key_regex = "^ship_.*$"`
     */
    val typeKeyRegex: CwtOptionDataAccessor<Regex?> by create {
        findOption("type_key_regex")?.stringValue?.toRegex(RegexOption.IGNORE_CASE)
    }

    /**
     * 类型键的前缀要求（忽略大小写）。
     *
     * 要求类型键必须以指定前缀开始。
     *
     * 适用对象：类型规则（[CwtTypeConfig]）与子类型规则（[CwtSubtypeConfig]）。
     *
     * CWTools 兼容性：兼容。
     *
     * 示例：`## starts_with = ship_`
     */
    val startsWith: CwtOptionDataAccessor<String?> by create {
        findOption("starts_with")?.stringValue
    }

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
    val onlyIfNot: CwtOptionDataAccessor<Set<String>?> by create {
        val r = findOption("only_if_not")?.getOptionValueOrValues()
        r?.optimized()
    }

    /**
     * 图相关的关联类型集合（graph_related_types）。
     *
     * 用于启用并扩充类型图视图（Graph View），以便联动显示相关类型。
     *
     * 示例：`## graph_related_types = { special_project anomaly_category }`
     *
     * CWTools 兼容性：PLS 未实现相关功能。
     */
    val graphRelatedTypes: CwtOptionDataAccessor<Set<String>?> by create {
        val r = findOption("graph_related_types")?.getOptionValues()
        r?.optimized()
    }

    /**
     * 允许的文件扩展名集合（file_extensions）。
     *
     * 用于约束路径引用，从而提供代码检查与过滤代码补全。
     *
     * 适用对象：值为路径引用（参见 [CwtDataTypeGroups.PathReference]）的成员规则（[CwtMemberConfig]）。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## file_extensions = { png dds tga }`
     *
     * @see icu.windea.pls.lang.inspections.script.common.IncorrectPathReferenceInspection
     * @see icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager.completePathReference
     */
    val fileExtensions: CwtOptionDataAccessor<Set<String>> by create(cached = true) {
        val r = findOption("file_extensions")?.getOptionValueOrValues()
        r?.optimized().orEmpty()
    }

    /**
     * 修正分类键集合（modifier_categories）。
     *
     * 脚本化修正分类用到的类别键集合，驱动补全、分组与展示。
     *
     * CWTools 兼容性：PLS 扩展。主要供插件逻辑使用。
     *
     * 示例：`## modifier_categories = { economic_unit planet }`
     *
     * @see icu.windea.pls.lang.util.ParadoxModifierManager.resolveModifierCategory
     */
    val modifierCategories: CwtOptionDataAccessor<Set<String>?> by create(cached = true) {
        val r = findOption("modifier_categories")?.getOptionValues()
        r?.optimized()
    }

    /**
     * 颜色类型（`hex` / `rgb` / `hsv` / `hsv360`）。
     *
     * 指定颜色值的解析/渲染模式。
     *
     * 适用对象：携带了颜色信息的定义成员对应的规则。
     *
     * CWTools 兼容性：PLS 扩展。用于增强编辑体验（拾色器、渲染、格式化）。
     *
     * 示例：`## color_type = rgb`
     *
     * @see icu.windea.pls.lang.util.ParadoxColorManager.getColorType
     */
    val colorType: CwtOptionDataAccessor<String?> by create {
        findOption("color_type")?.stringValue
    }

    /**
     * 检查抑制集合（suppress）。
     *
     * 通过若干 `## suppress = TOOL_ID` 来禁用对应 ID 的代码检查。
     *
     * 适用对象：定义成员对应的规则。
     *
     * CWTools 兼容性：PLS 未启用相关功能。
     *
     * 示例：`## suppress = ParadoxScriptUnresolvedExpression`
     *
     * TODO 暂未使用，需要验证
     *
     * @see icu.windea.pls.lang.inspections.suppress.ParadoxScriptConfigAwareInspectionSuppressor
     */
    val suppressSet: CwtOptionDataAccessor<Set<String>> by create {
        val r = findOptions("suppress").mapNotNullTo(FastSet()) { it.stringValue }
        r.optimized()
    }

    /**
     * 要注入从而成为当前成员规则的子规则的一组成员规则的路径。
     *
     * 适用对象：任意值为子句的成员规则。
     *
     * CWTools 兼容性：PLS 扩展。
     *
     * 示例：`## inject = some/file.cwt@some/property`
     *
     * @see icu.windea.pls.ep.config.CwtInjectConfigPostProcessor
     */
    val inject: CwtOptionDataAccessor<String?> by create {
        findOption("inject")?.stringValue
    }
}
