package icu.windea.pls.core.search.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.model.*

typealias ParadoxInlineScriptSelector = ChainedParadoxSelector<ParadoxInlineScriptUsageInfo>

fun inlineScriptSelector(project: Project, context: Any? = null) = ParadoxInlineScriptSelector(project, context)