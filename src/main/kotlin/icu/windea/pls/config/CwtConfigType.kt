package icu.windea.pls.config

import icu.windea.pls.core.*
import javax.swing.*

/**
 * @see CwtConfigTypes
 */
data class CwtConfigType(
    val id: String,
    val category: String? = null,
    val isReference: Boolean = false,
    val prefix: String? = null,
    val description: String? = null,
    val icon: Icon? = null,
) {
    fun getShortName(name: String): String {
        //简单判断
        return when (this) {
            CwtConfigTypes.Type, CwtConfigTypes.Subtype -> name.substringIn('[', ']')
            CwtConfigTypes.Enum, CwtConfigTypes.ComplexEnum, CwtConfigTypes.DynamicValueType -> name.substringIn('[', ']')
            CwtConfigTypes.Inline -> name.substringIn('[', ']')
            CwtConfigTypes.SingleAlias -> name.substringIn('[', ']')
            CwtConfigTypes.Alias, CwtConfigTypes.Trigger, CwtConfigTypes.Effect -> name.substringIn('[', ']').substringAfter(':')
            else -> name
        }
    }

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
        class Builder {
            var prefix: String? = null
            var description: String? = null
            var icon: Icon? = null
        }

        inline operator fun invoke(
            id: String,
            category: String? = null,
            isReference: Boolean = false,
            builder: Builder.() -> Unit
        ): CwtConfigType {
            val b = Builder().also(builder)
            return CwtConfigType(id, category, isReference, b.prefix, b.description, b.icon)
        }
    }
}
