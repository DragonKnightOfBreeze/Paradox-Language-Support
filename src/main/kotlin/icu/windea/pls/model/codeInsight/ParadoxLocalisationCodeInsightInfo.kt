package icu.windea.pls.model.codeInsight

import icu.windea.pls.config.config.*
import icu.windea.pls.model.*

data class ParadoxLocalisationCodeInsightInfo(
    val type: Type,
    val name: String?,
    val relatedLocalisationInfo: ParadoxDefinitionInfo.RelatedLocalisationInfo?,
    val locale: CwtLocaleConfig,
    val check: Boolean,
    val missing: Boolean,
    val dynamic: Boolean
) {
    enum class Type {
        Required, Primary, Optional,
        GeneratedModifierName, GeneratedModifierDesc,
        ModifierName, ModifierDesc,
        Reference
    }

    val key = when {
        relatedLocalisationInfo != null -> "@${relatedLocalisationInfo.key}@${locale.id}"
        name != null -> "$name@${locale.id}"
        else -> null
    }
}
