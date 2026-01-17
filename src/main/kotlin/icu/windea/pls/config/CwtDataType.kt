package icu.windea.pls.config

/**
 * 数据类型。
 *
 * 用于描述脚本文件中的表达式（键或值）的取值形态，可为常量、模式、基本数据类型、引用、复杂表达式等情况。
 *
 * @see CwtDataTypes
 * @see CwtDataTypeSets
 */
data class CwtDataType(
    val id: String,
    val isReference: Boolean = false,
    val isPatternAware: Boolean = false,
    val isSuffixAware: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        return this === other || (other is CwtDataType && id == other.id)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "CwtDataType($id)"
    }
}
