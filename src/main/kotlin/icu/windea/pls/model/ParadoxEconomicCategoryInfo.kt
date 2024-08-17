package icu.windea.pls.model

import icu.windea.pls.core.annotations.*

/**
 * @property modifierCategory modifier_category属性的值，并非modifier_categories.cwt中已定义的值
 */
@WithGameType(ParadoxGameType.Stellaris)
data class ParadoxEconomicCategoryInfo(
    val name: String,
    val parent: String? = null,
    val useForAiBudget: Boolean = false,
    val modifierCategory: String? = null,
    val parents: Set<String> = emptySet(),
    val modifiers: Set<ParadoxEconomicCategoryInfo.ModifierInfo> = emptySet(),
) {
    override fun equals(other: Any?): Boolean {
        return this === other || (other is ParadoxEconomicCategoryInfo && name == other.name)
    }
    
    override fun hashCode(): Int {
        return name.hashCode()
    }
    
    @WithGameType(ParadoxGameType.Stellaris)
    data class ModifierInfo(
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
                if(key != null) append(key).append("_")
                if(resource != null) append(resource).append("_")
                append(category).append("_").append(type)
            }
        }
        
        override fun equals(other: Any?): Boolean {
            return this === other || (other is ParadoxEconomicCategoryInfo && name == other.name)
        }
        
        override fun hashCode(): Int {
            return name.hashCode()
        }
    }
}
