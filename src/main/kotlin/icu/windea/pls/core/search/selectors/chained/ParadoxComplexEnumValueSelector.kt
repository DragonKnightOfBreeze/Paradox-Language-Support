package icu.windea.pls.core.search.selectors.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxComplexEnumValueSelector(project: Project, context: Any? = null) : ChainedParadoxSelector<ParadoxScriptStringExpressionElement>(project, context)

fun complexEnumValueSelector(project: Project, context: Any? = null) = ParadoxComplexEnumValueSelector(project, context)

fun ParadoxComplexEnumValueSelector.distinctByName() =
    distinctBy { ParadoxComplexEnumValueHandler.getName(it) }