package icu.windea.pls.model.codeInsight

import icu.windea.pls.model.*

data class ParadoxImageCodeInsightInfo(
    val type: Type,
    val filePath: String?,
    val gfxName: String?,
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
    
    val key = when {
        relatedImageInfo != null -> "@" + relatedImageInfo.key
        filePath != null -> filePath
        gfxName != null -> "#" + gfxName
        else -> null
    }
}
