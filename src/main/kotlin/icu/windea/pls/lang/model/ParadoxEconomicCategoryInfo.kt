package icu.windea.pls.lang.model

import icu.windea.pls.core.annotations.*

/**
 * @property modifierCategory modifier_category属性的值，并非modifier_categories.cwt中已定义的值
 */
@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxEconomicCategoryInfo(
    val name: String,
    val parent: String? = null,
    val useForAiBudget: Boolean = false,
    val modifiers: Set<ParadoxEconomicCategoryModifierInfo> = emptySet(),
    val modifierCategory: String? = null,
)

@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxEconomicCategoryModifierInfo(
    val name: String,
    val resource: String?,
    val triggered: Boolean = false,
    val useParentIcon: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxEconomicCategoryInfo && name == other.name)
    }
    
    override fun hashCode(): Int {
        return name.hashCode()
    }
}

@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxTriggeredModifierInfo(
    val key: String,
    val useParentIcon: Boolean = false,
    val modifierTypes: List<String>
)