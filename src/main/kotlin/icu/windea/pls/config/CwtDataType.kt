package icu.windea.pls.config

import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup

/**
 * 数据类型。
 *
 * 用于描述脚本文件中的表达式（键或值）的取值形态，可为常量、模式、基本数据类型、引用、复杂表达式等情况。
 *
 * @property priority 优先级。脚本表达式会优先匹配优先级更高的数据表达式。
 * @property priorityProvider 动态获取的优先级。脚本表达式会优先匹配优先级更高的数据表达式。
 *
 * @see CwtDataTypes
 * @see CwtDataTypeSets
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
