package icu.windea.pls.config.util.data

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.options
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.config.util.data.CwtOptionDataAccessors.pushScope
import icu.windea.pls.config.util.data.CwtOptionDataAccessors.replaceScopes
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.ParadoxScopeContext
import icu.windea.pls.model.resolve
import icu.windea.pls.model.resolveNext

/**
 * 提供常见的选项数据访问器。
 *
 * @see CwtOptionDataAccessor
 */
object CwtOptionDataAccessors : CwtOptionDataAccessorExtensionsAware {
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T> create(cached: Boolean = false, noinline action: CwtMemberConfig<*>.() -> T): CwtOptionDataAccessorProvider<T> {
        return CwtOptionDataAccessorProvider(cached, action)
    }

    /**
     * 允许的出现次数范围。
     * - 适用于：定义成员对应的规则。
     * - 示例：`## cardinality = 0..1`
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
     * 允许的最小出现次数。从预设值动态获取。
     * - 适用于：定义成员对应的规则。
     * - 示例：`## cardinality_min_define = "NGameplay/ETHOS_MIN_POINTS"`
     */
    val cardinalityMinDefine: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("cardinality_min_define")?.stringValue
    }

    /**
     * 允许的最大出现次数。从预设值动态获取。
     * - 适用于：定义成员对应的规则。
     * - 示例：`## cardinality_max_define = "NGameplay/ETHOS_MAX_POINTS"`
     */
    val cardinalityMaxDefine: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("cardinality_max_define")?.stringValue
    }

    /**
     * 在匹配规则时，通过简单的结构匹配进行过滤。
     * - 适用于：定义成员对应的规则。
     * - 示例：`## predicate = { scope = fleet type != country }`
     */
    val predicate: CwtOptionDataAccessor<Map<String, ReversibleValue<String>>> by create(cached = true) {
        val option = findOption("predicate") ?: return@create emptyMap()
        option.options?.associate { it.key to ReversibleValue(it.separatorType == CwtSeparatorType.EQUAL, it.value) }.orEmpty()
    }

    /**
     * 用于改变作用域上下文（替换）。
     * - 适用于：各种可存在作用域上下文的规则。
     * - 示例：`## replace_scopes = { this = country root = country }`
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
     * 用于改变作用域上下文（入栈）。
     * - 适用于：各种可存在作用域上下文的规则。
     * - 示例：`## push_scope = country`
     */
    val pushScope: CwtOptionDataAccessor<String?> by create(cached = true) {
        val option = findOption("push_scope")
        option?.getOptionValue()?.let { v -> ParadoxScopeManager.getScopeId(v) }
    }

    /**
     * 初始的作用域上下文。来自 [replaceScopes] 与 [pushScope]，经过计算得到。
     * - 适用于：各种可存在作用域上下文的规则。
     */
    val scopeContext: CwtOptionDataAccessor<ParadoxScopeContext?> by create(cached = true) {
        val replaceScopes = optionData { replaceScopes }
        val pushScope = optionData { pushScope }
        val scopeContext = replaceScopes?.let { ParadoxScopeContext.resolve(it) }
        scopeContext?.resolveNext(pushScope) ?: pushScope?.let { ParadoxScopeContext.resolve(it, it) }
    }

    /**
     * 支持的作用域。默认支持任意作用域。
     * - 适用于：各种可存在作用域上下文的规则。
     * - 示例：`## scopes = { country }`
     */
    val supportedScopes: CwtOptionDataAccessor<Set<String>> by create(cached = true) {
        // ignore case for scopes (to lowercase)
        val option = findOption("scope", "scopes")
        val r = option?.getOptionValueOrValues()?.mapTo(mutableSetOf()) { ParadoxScopeManager.getScopeId(it) }
        if (r.isNullOrEmpty()) ParadoxScopeManager.anyScopeIdSet else r
    }

    /**
     * 类型。（常用于扩展规则的通用标识）
     * - 示例：`## type = some_type`
     */
    val type: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("type")?.stringValue
    }

    /**
     * 提示信息。（常用于 QuickDoc 等展示）
     * - 示例：`## hint = "some hint"`
     */
    val hint: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("hint")?.stringValue
    }

    /**
     * 事件类型。（用于 on_action 等扩展规则）
     * - 示例：`## event_type = country_event`
     */
    val eventType: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("event_type")?.stringValue
    }

    /**
     * 上下文键。（用于参数等场景的上下文定位）
     * - 示例：`## context_key = parameter_context`
     */
    val contextKey: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("context_key")?.stringValue
    }

    /**
     * 上下文配置的类型。若未指定，默认返回 "single"。
     * - 示例：`## context_configs_type = multiple`
     */
    val contextConfigsType: CwtOptionDataAccessor<String> by create(cached = true) {
        findOption("context_configs_type")?.stringValue ?: "single"
    }

    /**
     * 分组名。（用于子类型分组等）
     * - 示例：`## group = ships`
     */
    val group: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("group")?.stringValue
    }

    /**
     * 搜索范围类型。（用于复杂枚举值等检索）
     * - 示例：`## search_scope_type = project`
     */
    val searchScopeType: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("search_scope_type")?.stringValue
    }

    /**
     * 类型键过滤器。（忽略大小写；由分隔符决定正/反含义）
     * - 示例：`## type_key_filter = { ship country }`
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
     * 类型键的正则过滤器。（忽略大小写）
     * - 示例：`## type_key_regex = "^ship_.*$"`
     */
    val typeKeyRegex: CwtOptionDataAccessor<Regex?> by create(cached = true) {
        findOption("type_key_regex")?.stringValue?.toRegex(RegexOption.IGNORE_CASE)
    }

    /**
     * 名称前缀要求。
     * - 示例：`## starts_with = ship_`
     */
    val startsWith: CwtOptionDataAccessor<String?> by create(cached = true) {
        // 不忽略大小写
        findOption("starts_with")?.stringValue
    }

    /**
     * 排除名单（只要不在此集合中才匹配）。
     * - 示例：`## only_if_not = { simple complex }`
     */
    val onlyIfNot: CwtOptionDataAccessor<Set<String>?> by create(cached = true) {
        findOption("only_if_not")?.getOptionValueOrValues()
    }

    /**
     * 关联的类型集合（图相关）。
     * - 示例：`## graph_related_types = { a b c }`
     */
    val graphRelatedTypes: CwtOptionDataAccessor<Set<String>?> by create(cached = true) {
        findOption("graph_related_types")?.getOptionValues()
    }

    /**
     * 允许的文件扩展名集合。
     * - 示例：`## file_extensions = { txt md }`
     */
    val fileExtensions: CwtOptionDataAccessor<Set<String>> by create(cached = true) {
        findOption("file_extensions")?.getOptionValueOrValues().orEmpty()
    }

    /**
     * 修正类别键集合。（用于脚本化修正类别）
     * - 示例：`## modifier_categories = { economic_unit planet }`
     */
    val modifierCategories: CwtOptionDataAccessor<Set<String>?> by create(cached = true) {
        findOption("modifier_categories")?.getOptionValues()
    }

    /**
     * 颜色类型。（用于颜色值的解析与渲染）
     * 可选值示例：`hex` `rgb` `hsv` `hsv360`。
     * - 示例：`## color_type = rgb`
     */
    val colorType: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("color_type")?.stringValue
    }

    /**
     * 抑制的检查 ID 集合。（用于根据规则禁用特定 Inspection）
     * - 示例：`## suppress = ParadoxScriptUnresolvedExpression`
     */
    val suppressSet: CwtOptionDataAccessor<Set<String>> by create(cached = true) {
        findOptions("suppress").mapNotNullTo(mutableSetOf()) { it.stringValue }
    }

    /**
     * 内联脚本表达式的路径字段位置。（用于 inline_script 的扩展规则）
     * - 示例：`## inline_script_expression = "some/path"`
     */
    val inlineScriptExpression: CwtOptionDataAccessor<String?> by create(cached = true) {
        findOption("inline_script_expression")?.stringValue
    }
}
