package icu.windea.pls.model.codeInsight

import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*

data class ParadoxLocalisationCodeInsightInfo(
    val type: Type,
    val name: String?,
    val relatedLocalisationInfo: ParadoxDefinitionRelatedLocalisationInfo?,
    val locale: CwtLocalisationLocaleConfig,
    val check: Boolean,
    val missing: Boolean,
    val dynamic: Boolean
) {
    enum class Type {
        Required, Primary, Optional,
        GeneratedModifierName, GeneratedModifierDesc,
        ModifierName, ModifierDesc
    }
    
    fun getMissingMessage(): String? {
        return null //TODO
    }
}