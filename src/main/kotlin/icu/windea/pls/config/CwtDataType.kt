package icu.windea.pls.config

import icu.windea.pls.config.CwtDataType.Companion.entries
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.ep.config.configExpression.CwtDataExpressionResolver
import icu.windea.pls.ep.match.ParadoxScriptExpressionMatcher
import icu.windea.pls.lang.match.ParadoxMatchResult

/**
 * 数据类型。
 *
 * 用于描述规则表达式（键或值）的取值形态，可为常量、模式、基本数据类型、引用、复杂表达式等情况。
 * 每种数据类型表示一种语义范畴，脚本表达式与规则表达式之间的匹配由数据类型驱动。
 *
 * ### 解析逻辑
 *
 * 规则表达式的字符串由 [CwtDataExpressionResolver] 解析为 [CwtDataExpression]，
 * 其中包含对应的数据类型。解析器按扩展点注册顺序依次尝试：
 *
 * - **基本类型**：字面量匹配（如 `bool`→[CwtDataTypes.Bool]）或带范围参数（如 `int[0..100]` → [CwtDataTypes.Int]）。
 * - **核心类型**：参数化匹配（如 `<type>`→[CwtDataTypes.Definition]，`enum[name]` → [CwtDataTypes.EnumValue]）。
 * - **模板表达式**：包含引用占位符的模式（如 `a_<b>_enum[c]`→[CwtDataTypes.TemplateExpression]）。
 * - **Ant模式**：`ant:` 或 `ant.i:`前缀（→[CwtDataTypes.Ant]）。
 * - **正则模式**：`re:` 或 `re.i:` 前缀（→[CwtDataTypes.Regex]）。
 * - **后缀感知类型**：含 `|` 分隔的后缀列表（如 `<type>|suffix1,suffix2`→[CwtDataTypes.SuffixAwareDefinition]）。
 * - **常量**：不含特殊字符的普通字符串（→ [CwtDataTypes.Constant]）。
 *
 * ### 匹配逻辑
 *
 * 脚本表达式与规则表达式的匹配由 [ParadoxScriptExpressionMatcher]
 * 驱动，根据规则表达式的数据类型分派到对应的匹配分支。匹配结果为 [ParadoxMatchResult]，
 * 存在多个候选规则时优先选择 [priority] 更高的数据表达式。
 *
 * ### 数据类型分类
 *
 * - **模式感知**（[isPatternAware]）：表达式自身包含文本模式，匹配时进行模式比较（如常量精确匹配、Ant模式、正则匹配）。
 * - **后缀感知**（[isSuffixAware]）：表达式由基础引用和后缀列表组成，匹配时需同时验证引用和后缀。
 * - **引用类型**（[isReference]）：表达式指向可导航的目标（如定义、本地化、文件路径等）。
 *
 * ### 备注
 *
 * 为优化性能，此类使用引用相等（identity equality）而非结构相等。所有实例通过 [Builder] 构建并注册到 [entries] 中。
 *
 * @property id 唯一标识符。
 * @property isReference 是否表示一个可导航的引用。
 * @property isPatternAware 是否为模式感知类型（表达式包含文本模式）。
 * @property isSuffixAware 是否为后缀感知类型（表达式包含后缀列表）。
 * @property priority 静态优先级。脚本表达式会优先匹配优先级更高的数据表达式。
 * @property priorityProvider 动态优先级提供者。根据具体的数据表达式和规则组动态计算优先级。
 *
 * @see CwtDataTypes
 * @see CwtDataTypeSets
 * @see CwtDataExpression
 * @see CwtDataExpressionResolver
 * @see ParadoxScriptExpressionMatcher
 */
@Suppress("unused")
class CwtDataType private constructor(
    val id: String,
    val isReference: Boolean = false,
    val isPatternAware: Boolean = false,
    val isSuffixAware: Boolean = false,
    val priority: Double? = null,
    val priorityProvider: ((CwtDataExpression, CwtConfigGroup) -> Double)? = null,
) {
    // NOTE 2.1.1 为了优化性能，这里直接使用引用相等
    // override fun equals(other: Any?) = super.equals(other)
    //
    // override fun hashCode() = super.hashCode()

    override fun toString() = "CwtDataType(id=$id)"

    class Builder(
        private val id: String
    ) {
        private var isReference: Boolean = false
        private var isPatternAware: Boolean = false
        private var isSuffixAware: Boolean = false
        var priority: Double? = null
        var priorityProvider: ((CwtDataExpression, CwtConfigGroup) -> Double)? = null

        fun reference() = apply { isReference = true }
        fun patternAware() = apply { isPatternAware = true }
        fun suffixAware() = apply { isSuffixAware = true }
        fun withPriority(value: Double) = apply { priority = value }
        fun withPriority(value: (CwtDataExpression, CwtConfigGroup) -> Double) = apply { priorityProvider = value }

        fun build(): CwtDataType = CwtDataType(id, isReference, isPatternAware, isSuffixAware, priority, priorityProvider).also { _entries[id] = it }
    }

    companion object {
        private val _entries = mutableMapOf<String, CwtDataType>()

        @JvmStatic
        val entries: Map<String, CwtDataType> get() = _entries

        @JvmStatic
        fun builder(id: String): Builder = Builder(id)
    }
}
