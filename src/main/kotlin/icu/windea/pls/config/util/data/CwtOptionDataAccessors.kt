package icu.windea.pls.config.util.data

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.options
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtCardinalityExpression
import icu.windea.pls.config.util.data.CwtOptionDataAccessors.pushScope
import icu.windea.pls.config.util.data.CwtOptionDataAccessors.replaceScopes
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
}
