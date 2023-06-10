package icu.windea.pls.core.search.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.model.*

class ParadoxValueSetValueSelector(project: Project, context: Any? = null): ChainedParadoxSelector<ParadoxValueSetValueInfo>(project, context)

fun valueSetValueSelector(project: Project, context: Any? = null) = ParadoxValueSetValueSelector(project, context)

fun ParadoxValueSetValueSelector.distinctByName() = distinctBy { it.name }