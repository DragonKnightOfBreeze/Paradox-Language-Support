package icu.windea.pls.config.core.config

import icu.windea.pls.core.annotations.*

/**
 * @property modifierCategory modifier_category属性的值，并非modifier_categories.cwt中已定义的值
 */
@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxEconomicCategoryInfo(
    val name: String,
    val parent: String?,
    val useForAiBudget: Boolean,
    val modifiers: Set<ParadoxEconomicCategoryModifierInfo>,
    val modifierCategory: String?,
)

@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxEconomicCategoryModifierInfo(
    val name: String,
    val aiBudget: Boolean,
    val triggered: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxEconomicCategoryInfo && name == other.name)
    }
    
    override fun hashCode(): Int {
        return name.hashCode()
    }
}