package icu.windea.pls.config

/**
 * @see CwtDataTypes
 * @see CwtDataTypeGroups
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
