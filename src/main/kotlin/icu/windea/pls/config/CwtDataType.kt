package icu.windea.pls.config

/**
 * 数据类型。
 *
 * 用于描述脚本文件中的表达式（键或值）的取值形态，可为常量、模式、基本数据类型、引用、复杂表达式等情况。
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
) {
    // NOTE 2.1.1 为了优化性能，这里直接使用引用相等
    // override fun equals(other: Any?) = super.equals(other)
    //
    // override fun hashCode() = super.hashCode()

    override fun toString() = "CwtDataType(id=$id)"

    class Builder(
        private val id: String,
        private var isReference: Boolean = false,
        private var isPatternAware: Boolean = false,
        private var isSuffixAware: Boolean = false,
    ) {
        fun reference() = apply { isReference = true }
        fun patternAware() = apply { isPatternAware = true }
        fun suffixAware() = apply { isSuffixAware = true }

        fun build(): CwtDataType = CwtDataType(id, isReference, isPatternAware, isSuffixAware).also { _entries[id] = it }
    }

    companion object {
        private val _entries = mutableMapOf<String, CwtDataType>()

        @JvmStatic
        val entries: Map<String, CwtDataType> get() = _entries

        @JvmStatic
        fun builder(id: String): Builder = Builder(id)
    }
}
