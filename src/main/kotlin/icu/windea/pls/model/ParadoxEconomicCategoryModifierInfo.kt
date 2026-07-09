package icu.windea.pls.model

import icu.windea.pls.base.annotations.WithGameType

@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxEconomicCategoryModifierInfo(
    val key: String,
    val resource: String?,
    val category: String,
    val type: String,
    val triggered: Boolean = false,
    val useParentIcon: Boolean = false,
) {
    val name = resolveName(key)

    fun resolveName(key: String?): String {
        return buildString {
            if (key != null) append(key).append("_")
            if (resource != null) append(resource).append("_")
            append(category).append("_").append(type)
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxEconomicCategoryModifierInfo && name == other.name)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
