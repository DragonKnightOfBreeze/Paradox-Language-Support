package icu.windea.pls.config.util.data

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.options
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.ParadoxScopeContext
import icu.windea.pls.model.resolve
import icu.windea.pls.model.resolveNext

/**
 * 提供“选项数据访问器”（Option Data Accessors）的集中定义与实现。
 *
 * 用途与行为（重要）：
 * - 这些 accessor 以统一、类型安全、带缓存的方式，从某个成员规则（`CwtMemberConfig`）附带的选项（`CwtOptionMemberConfig`）里提取所需的数据。
 * - 统一的调用方式为：`config.optionData { someAccessor }`。
 * - 大多数 accessor 都内建缓存（`create(cached = true)`），避免在解析、补全等流程中重复解析选项。
 * - 对于作用域相关的选项（如 `replace_scopes`、`push_scope`、`scopes`），PLS 会做大小写与别名归一化处理（参见 `ParadoxScopeManager`）。
 * - 部分 accessor 是 PLS 的“扩展选项”（CWTools 原版不支持或语义不同），其差异会在各自的 KDoc 中明确说明。
 *
 * 参考：
 * - CWTools 指引：`references/cwt/guidance.md`
 * - PLS 配置说明：`docs/zh/config.md`
 * - 规则示例：`cwt/` 目录下内置与远端导入的 `.cwt` 文件
 *
 * @see CwtOptionDataAccessor
 */
object CwtOptionDataAccessors : CwtOptionDataAccessorExtensionsAware {
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> create(cached: Boolean = false, noinline action: CwtMemberConfig<*>.() -> T): CwtOptionDataAccessorProvider<T> {
        return CwtOptionDataAccessorProvider(cached, action)
    }

    /**
     * 允许的出现次数范围（基数表达式）。
     *
     * 描述与用处：
     * - 指定后续规则项的匹配次数上下限，形如 `min..max`，其中 `max` 可为 `inf` 或 `~1` 等形式。
     * - 若未显式指定且该成员为“常量类型”，PLS 默认推断为 `1..~1`（必须出现一次，缺失仅做提示）。
     *
     * 适用对象：
     * - 绝大多数成员规则（属性、值、别名等）
     *
     * 示例（.cwt）：
     * - `## cardinality = 0..1`
     *
     * 差异说明：
     * - 与 CWTools 保持一致；默认推断逻辑为 PLS 的增强（参考 `docs/zh/config.md`）。
     */
    val cardinality: CwtOptionDataAccessor<CwtCardinalityExpression?> by create(cached = true) {
        val option = findOption("cardinality")
        if (option == null) {
            // 如果没有注明且类型是常量，则推断为 1..~1
            if (configExpression.type == CwtDataTypes.Constant) {
                return@create CwtCardinalityExpression.resolve("1..~1")
            }
        }
        option?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
    }

    /**
     * 最小基数（从 Define 预设动态获取）。
     *
     * 描述与用处：
     * - 通过字符串路径（如 `NGameplay/ETHOS_MIN_POINTS`）动态指定最小次数。
     *
     * 示例（.cwt）：
     * - `## cardinality_min_define = "NGameplay/ETHOS_MIN_POINTS"`
     *
     * 差异说明：
     * - 这是 PLS 的扩展选项，CWTools 原版未内置该语义（仅作为注释文本）。PLS 会在 QuickDoc、校验等处使用该信息。
     */
    val cardinalityMinDefine: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("cardinality_min_define")?.stringValue
    }

    /**
     * 最大基数（从 Define 预设动态获取）。
     *
     * 示例（.cwt）：
     * - `## cardinality_max_define = "NGameplay/ETHOS_MAX_POINTS"`
     *
     * 差异说明：
     * - PLS 扩展；语义同上。
     */
    val cardinalityMaxDefine: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("cardinality_max_define")?.stringValue
    }

    /**
     * 简单结构谓词（结构化过滤）。
     *
     * 描述与用处：
     * - 使用 `key = value` 或 `key != value` 的形式对匹配进行简单过滤。
     * - 值存储为 `ReversibleValue`，其中分隔符 `=` 表示正向、`!=` 表示反向。
     *
     * 适用对象：
     * - 一般成员规则，作为补充过滤条件。
     *
     * 示例（.cwt）：
     * - `## predicate = { scope = fleet type != country }`
     *
     * 差异说明：
     * - 该选项在 CWTools 文档中并非核心项；PLS 将其作为扩展/约定选项，用于特定的过滤逻辑。
     */
    val predicate: CwtOptionDataAccessor<Map<String, ReversibleValue<String>>> by create(cached = true) {
        val option = findOption("predicate") ?: return@create emptyMap()
        option.options?.associate { it.key to ReversibleValue(it.separatorType == CwtSeparatorType.EQUAL, it.value) }.orEmpty()
    }

    /**
     * 替换作用域上下文（replace scopes）。
     *
     * 描述与用处：
     * - 将当前上下文的 `this/root/from/...` 映射到指定的作用域 ID。
     * - 支持键名 `replace_scope` 与 `replace_scopes`，二者等价。
     * - 值中的作用域会被 PLS 归一化（大小写与别名）。
     *
     * 适用对象：
     * - 任何存在“作用域上下文”的规则（如定义、别名、扩展规则等）。
     *
     * 示例（.cwt）：
     * - `## replace_scopes = { this = country root = country }`
     *
     * 差异说明：
     * - 与 CWTools 兼容；PLS 额外支持复数写法与归一化处理。
     */
    val replaceScopes: CwtOptionDataAccessor<Map<String, String>?> by create(cached = true) {
        val option = findOption("replace_scope", "replace_scopes")
        if (option == null) return@create null
        val options1 = option.options ?: return@create null
        buildMap {
            for (option1 in options1) {
                // ignore case for both system scopes and scopes (to lowercase)
                val k = option1.key.lowercase()
                val v = option1.stringValue?.let { ParadoxScopeManager.getScopeId(it) } ?: continue
                put(k, v)
            }
        }
    }

    /**
     * 入栈作用域（push scope）。
     *
     * 描述与用处：
     * - 将指定作用域（通常为 `this`）推入作用域栈，影响后续规则匹配的上下文。
     * - 返回值为归一化后的作用域 ID。
     *
     * 示例（.cwt）：
     * - `## push_scope = country`
     *
     * 差异说明：
     * - 与 CWTools 保持一致；PLS 会做作用域 ID 归一化。
     */
    val pushScope: CwtOptionDataAccessor<String?> by create(cached = true) {
        val option = findOption("push_scope")
        option?.getOptionValue()?.let { v -> ParadoxScopeManager.getScopeId(v) }
    }

    /**
     * 计算后的“初始作用域上下文”。
     *
     * 描述与用处：
     * - 基于 `replaceScopes` 与 `pushScope` 计算得到 `ParadoxScopeContext`。
     * - 用于后续表达式匹配、补全与校验。
     */
    val scopeContext: CwtOptionDataAccessor<ParadoxScopeContext?> by create(cached = true) {
        val replaceScopes = optionData { replaceScopes }
        val pushScope = optionData { pushScope }
        val scopeContext = replaceScopes?.let { ParadoxScopeContext.resolve(it) }
        scopeContext?.resolveNext(pushScope) ?: pushScope?.let { ParadoxScopeContext.resolve(it, it) }
    }

    /**
     * 支持的作用域集合（默认支持任意）。
     *
     * 描述与用处：
     * - 通过 `scope = X` 或 `scopes = { X Y }` 指定可用作用域集合。
     * - PLS 将值转换为作用域 ID（小写），便于统一比对。
     *
     * 示例（.cwt）：
     * - `## scope = country`
     * - `## scopes = { country planet }`
     */
    val supportedScopes: CwtOptionDataAccessor<Set<String>> by create(cached = true) {
        // ignore case for scopes (to lowercase)
        val option = findOption("scope", "scopes")
        val r = option?.getOptionValueOrValues()?.mapTo(mutableSetOf()) { ParadoxScopeManager.getScopeId(it) }
        if (r.isNullOrEmpty()) ParadoxScopeManager.anyScopeIdSet else r
    }

    /**
     * 类型标识（多用于扩展规则）。
     *
     * 描述与用处：
     * - 在扩展规则（如 definitions、on_actions 等）中用来携带“类别信息”。
     *
     * 示例（.cwt）：
     * - `## type = scripted_trigger`
     *
     * 差异说明：
     * - PLS 扩展，用于统一在插件端进行类型分发与 UI 展示。
     */
    val type: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("type")?.stringValue
    }

    /**
     * 提示信息（用于 QuickDoc/Inlay 等展示）。
     *
     * 差异说明：
     * - PLS 扩展；CWTools 不消费该选项，PLS 用于内嵌提示与文档输出。
     */
    val hint: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("hint")?.stringValue
    }

    /**
     * 事件类型（on_action 扩展规则）。
     *
     * 描述与用处：
     * - 指示 on_action 的事件类型（如 `country`、`system`）。
     *
     * 差异说明：
     * - PLS 扩展；用于 on_action 相关的解析与显示。
     */
    val eventType: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("event_type")?.stringValue
    }

    /**
     * 上下文键（context key）。
     *
     * 描述与用处：
     * - 指定参数或内联脚本的“所属上下文”，常见形式：`definitionType@definitionName` 或 `inline_script@path`。
     * - PLS 也支持模板/正则等灵活匹配（详见 `docs/zh/config.md`）。
     *
     * 差异说明：
     * - PLS 扩展。
     */
    val contextKey: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("context_key")?.stringValue
    }

    /**
     * 上下文配置的聚合类型。
     *
     * 描述与用处：
     * - 指定 `x = {...}` 根下的“上下文配置”是单个（`single`）还是多个（`multiple`）。
     * - 未指定时默认 `single`。
     *
     * 差异说明：
     * - PLS 扩展，见 `inline_scripts`、`parameters` 章节。
     */
    val contextConfigsType: CwtOptionDataAccessor<String> by create(cached = true) {
        findOption("context_configs_type")?.stringValue ?: "single"
    }

    /**
     * 分组名（group）。
     *
     * 描述与用处：
     * - 常用于 `subtype[...]` 的分组管理，也可在 UI 展示中使用。
     *
     * 差异说明：
     * - PLS 扩展；CWTools 侧通常不消费该选项。
     */
    val group: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("group")?.stringValue
    }

    /**
     * 搜索范围类型（search scope type）。
     *
     * 描述与用处：
     * - 主要用于复杂枚举（complex_enum）等跨项目/多源检索的场景（如 `project`）。
     *
     * 差异说明：
     * - PLS 扩展，用于控制搜索的粒度或范围。
     */
    val searchScopeType: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("search_scope_type")?.stringValue
    }

    /**
     * 类型键过滤器（type_key_filter）。
     *
     * 描述与用处：
     * - 用于 `type[...]` 与 `subtype[...]` 的候选过滤，值可为单个或列表。忽略大小写。
     * - 分隔符决定“正/反”含义：`=` 为包含、`!=`（或 `<>`）为排除。
     *
     * 示例（.cwt）：
     * - `## type_key_filter = country_event`
     * - `## type_key_filter = { ship country }`
     *
     * 差异说明：
     * - 与 CWTools 指引一致，PLS 在实现层面进行了大小写与集合优化。
     */
    val typeKeyFilter: CwtOptionDataAccessor<ReversibleValue<Set<String>>?> by create(cached = true) {
        // 值可能是 string 也可能是 stringArray
        val option = findOption("type_key_filter") ?: return@create null
        val values = option.getOptionValueOrValues() ?: return@create null
        val set = caseInsensitiveStringSet().apply { addAll(values) } // 忽略大小写
        val positive = option.separatorType == CwtSeparatorType.EQUAL
        ReversibleValue(positive, set.optimized())
    }

    /**
     * 类型键正则过滤器（忽略大小写）。
     *
     * 示例（.cwt）：
     * - `## type_key_regex = "^ship_.*$"`
     */
    val typeKeyRegex: CwtOptionDataAccessor<Regex?> by create(cached = true) {
        findOption("type_key_regex")?.stringValue?.toRegex(RegexOption.IGNORE_CASE)
    }

    /**
     * 名称前缀要求（大小写敏感）。
     *
     * 描述与用处：
     * - 限定类型/子类型键必须以指定前缀开头。PLS 不做大小写归一化，按字面匹配。
     *
     * 示例（.cwt）：
     * - `## starts_with = ship_`
     */
    val startsWith: CwtOptionDataAccessor<String?> by create(cached = true) {
        // 不忽略大小写
        findOption("starts_with")?.stringValue
    }

    /**
     * 排除名单（only_if_not）。
     *
     * 描述与用处：
     * - 指定一个集合，只要“名称不在集合内”即可匹配。
     *
     * 示例（.cwt）：
     * - `## only_if_not = { simple complex }`
     */
    val onlyIfNot: CwtOptionDataAccessor<Set<String>?> by create(cached = true) {
        findOption("only_if_not")?.getOptionValueOrValues()
    }

    /**
     * 图相关的关联类型集合（graph_related_types）。
     *
     * 描述与用处：
     * - 用于启用并扩充类型图视图（Graph View），以便联动显示相关类型。
     *
     * 示例（.cwt）：
     * - `## graph_related_types = { special_project anomaly_category }`
     */
    val graphRelatedTypes: CwtOptionDataAccessor<Set<String>?> by create(cached = true) {
        findOption("graph_related_types")?.getOptionValues()
    }

    /**
     * 允许的文件扩展名集合（file_extensions）。
     *
     * 描述与用处：
     * - 用于文件引用/验证等功能，限制可接受的扩展名集合。
     *
     * 示例（.cwt）：
     * - `## file_extensions = { txt md }`
     *
     * 差异说明：
     * - PLS 扩展；CWTools 端一般不消费该选项。
     */
    val fileExtensions: CwtOptionDataAccessor<Set<String>> by create(cached = true) {
        findOption("file_extensions")?.getOptionValueOrValues().orEmpty()
    }

    /**
     * 修正类别键集合（modifier_categories）。
     *
     * 描述与用处：
     * - 脚本化修正类别用到的类别键集合，驱动补全、分组与展示。
     *
     * 示例（.cwt）：
     * - `## modifier_categories = { economic_unit planet }`
     *
     * 差异说明：
     * - PLS 扩展；该数据主要供插件逻辑使用（见 `ParadoxModifierManager`）。
     */
    val modifierCategories: CwtOptionDataAccessor<Set<String>?> by create(cached = true) {
        findOption("modifier_categories")?.getOptionValues()
    }

    /**
     * 颜色类型（`hex` / `rgb` / `hsv` / `hsv360`）。
     *
     * 描述与用处：
     * - 指定颜色值的解析/渲染模式（见 `ParadoxColorManager`）。
     *
     * 示例（.cwt）：
     * - `## color_type = rgb`
     *
     * 差异说明：
     * - PLS 扩展，用于增强编辑体验（拾色器、渲染、格式化）。
     */
    val colorType: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("color_type")?.stringValue
    }

    /**
     * 检查抑制集合（suppress）。
     *
     * 描述与用处：
     * - 通过若干 `## suppress = TOOL_ID` 来禁用特定 Inspection（如解析未决表达式）。
     * - PLS 将收集所有同名选项为集合。
     *
     * 示例（.cwt）：
     * - `## suppress = ParadoxScriptUnresolvedExpression`
     *
     * 差异说明：
     * - PLS 扩展；CWTools 不消费该选项。
     */
    val suppressSet: CwtOptionDataAccessor<Set<String>> by create(cached = true) {
        findOptions("suppress").mapNotNullTo(mutableSetOf()) { it.stringValue }
    }

    /**
     * 内联脚本表达式的“路径字段位置”。
     *
     * 描述与用处：
     * - 用于 `inline_scripts` 扩展规则，说明在上下文属性中哪个字段（路径）存放表达式字符串。
     *
     * 示例（.cwt）：
     * - `## inline_script_expression = "some/path"`
     *
     * 差异说明：
     * - PLS 扩展，用于推导与反向定位（见 `ParadoxInlineScriptManager`）。
     */
    val inlineScriptExpression: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("inline_script_expression")?.stringValue
    }
}
