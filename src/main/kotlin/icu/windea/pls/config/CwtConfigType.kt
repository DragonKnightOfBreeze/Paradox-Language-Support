package icu.windea.pls.config

import javax.swing.*

/**
 * @see CwtConfigTypes
 */
data class CwtConfigType(
    val id: String,
    val category: String? = null,
    val isReference: Boolean = false,
    val icon: Icon? = null,
    val prefix: String? = null,
    val description: String? = null,
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

    companion object {
        val entries = mutableMapOf<String, CwtConfigType>()

        class Builder {
            var icon: Icon? = null
            var prefix: String? = null
            var description: String? = null
        }

        fun create(id: String, category: String? = null, isReference: Boolean = false, builder: Builder.() -> Unit): CwtConfigType {
            val b = Builder().also(builder)
            val r = CwtConfigType(id, category, isReference, b.icon, b.prefix, b.description)
            entries[id] = r
            return r
        }
    }
}
