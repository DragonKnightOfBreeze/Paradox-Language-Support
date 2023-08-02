package icu.windea.pls.model.codeInsight

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.inspections.general.*
import icu.windea.pls.script.psi.*

data class ParadoxImageCodeInsightInfo(
    val type: Type,
    val filePath: String?,
    val gfxName: String?,
    val relatedImageInfo: ParadoxDefinitionRelatedImageInfo?,
    val check: Boolean,
    val missing: Boolean,
    val dynamic: Boolean
) {
    enum class ContextType {
        Definition,
        Modifier
    }
    
    enum class Type {
        Required, Primary, Optional,
        GeneratedModifierIcon,
        ModifierIcon
    }
    
    val key = when {
        relatedImageInfo != null -> "@${relatedImageInfo.key}"
        filePath != null -> filePath
        gfxName != null -> "#$gfxName"
        else -> null
    }
}
