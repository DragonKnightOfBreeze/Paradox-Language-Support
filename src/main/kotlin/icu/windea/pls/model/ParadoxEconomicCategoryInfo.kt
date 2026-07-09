package icu.windea.pls.model

import icu.windea.pls.base.annotations.WithGameType

/**
 * @property modifierCategory `modifier_category` 属性的值，而不是 `modifier_categories.cwt` 中已定义的值。
 */
@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxEconomicCategoryInfo(
    val name: String,
    val parent: String? = null,
    val useForAiBudget: Boolean = false,
    val modifierCategory: String? = null,
    val modifiers: Set<ParadoxEconomicCategoryModifierInfo> = emptySet(),
) {
    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxEconomicCategoryInfo && name == other.name)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
