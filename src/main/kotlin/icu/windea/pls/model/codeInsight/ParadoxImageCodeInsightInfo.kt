package icu.windea.pls.model.codeInsight

import icu.windea.pls.model.*

data class ParadoxImageCodeInsightInfo(
    val type: Type,
    val name: String?,
    val relatedImageInfo: ParadoxDefinitionRelatedImageInfo?,
    val check: Boolean,
    val missing: Boolean,
    val dynamic: Boolean
) {
    enum class Type {
        Required, Primary, Optional,
        GeneratedModifierIcon,
        ModifierIcon
    }
    
    fun getMissingMessage(): String? {
        return null //TODO
    }
}
