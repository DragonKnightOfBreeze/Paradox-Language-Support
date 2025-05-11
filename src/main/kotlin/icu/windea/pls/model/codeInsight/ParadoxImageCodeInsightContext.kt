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
import icu.windea.pls.script.psi.*

data class ParadoxImageCodeInsightContext(
    val type: Type,
    val name: String,
    val infos: List<ParadoxImageCodeInsightInfo>,
    val children: List<ParadoxImageCodeInsightContext> = emptyList(),
    val fromInspection: Boolean = false,
) {
    enum class Type {
        File,
        Definition,
        Modifier,
        Unresolved
    }
}
