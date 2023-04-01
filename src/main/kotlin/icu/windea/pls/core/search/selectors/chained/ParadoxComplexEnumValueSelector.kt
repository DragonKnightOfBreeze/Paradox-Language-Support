package icu.windea.pls.core.search.selectors.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.model.*

class ParadoxComplexEnumValueSelector(project: Project, context: Any? = null) : ChainedParadoxSelector<ParadoxComplexEnumValueInfo>(project, context)

fun complexEnumValueSelector(project: Project, context: Any? = null) = ParadoxComplexEnumValueSelector(project, context)
