package icu.windea.pls.model.codeInsight

import cn.yiiguxing.plugin.translate.util.elementType
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.inspections.script.common.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.*

data class ParadoxLocalisationCodeInsightContext(
    val type: Type,
    val name: String,
    val infos: List<ParadoxLocalisationCodeInsightInfo>,
    val children: List<ParadoxLocalisationCodeInsightContext> = emptyList(),
    val fromInspection: Boolean = false,
) {
    enum class Type {
        File,
        Definition,
        Modifier,
        LocalisationReference,
        SyncedLocalisationReference,
        Localisation,
    }
}
