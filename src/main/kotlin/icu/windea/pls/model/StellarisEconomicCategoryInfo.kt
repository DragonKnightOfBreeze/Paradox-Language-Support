package icu.windea.pls.model

import icu.windea.pls.core.annotations.*

/**
 * @property modifierCategory modifier_category属性的值，并非modifier_categories.cwt中已定义的值
 */
@WithGameType(ParadoxGameType.Stellaris)
data class StellarisEconomicCategoryInfo(
    val name: String,
    val parent: String? = null,
    val useForAiBudget: Boolean = false,
    val modifierCategory: String? = null,
    val modifiers: Set<StellarisEconomicCategoryModifierInfo> = emptySet(),
) {
    override fun equals(other: Any?): Boolean {
        return this === other || (other is StellarisEconomicCategoryInfo && name == other.name)
    }
    
    override fun hashCode(): Int {
        return name.hashCode()
    }
}

@WithGameType(ParadoxGameType.Stellaris)
data class StellarisEconomicCategoryModifierInfo(
    val key: String,
    val resource: String?,
    val category: String,
    val type: String,
    val triggered: Boolean = false,
    val useParentIcon: Boolean = false,
) {
    val name = when {
        resource == null -> "${key}_${category}_${type}"
        else -> "${key}_${resource}_${category}_${type}"
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || (other is StellarisEconomicCategoryInfo && name == other.name)
    }
    
    override fun hashCode(): Int {
        return name.hashCode()
    }
}