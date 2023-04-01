package icu.windea.pls.core.search.selectors.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.model.*

class ParadoxInlineScriptSelector(project: Project, context: Any? = null) : ChainedParadoxSelector<ParadoxInlineScriptInfo>(project, context)

fun inlineScriptSelector(project: Project, context: Any? = null) = ParadoxInlineScriptSelector(project, context)