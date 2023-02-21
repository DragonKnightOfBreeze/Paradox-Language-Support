package icu.windea.pls.core.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxComplexEnumValueSelector(project: Project) : ChainedParadoxSelector<ParadoxScriptStringExpressionElement>(project)

fun complexEnumValueSelector(project: Project) = ParadoxComplexEnumValueSelector(project)

fun ParadoxComplexEnumValueSelector.distinctByName() =
    distinctBy { ParadoxComplexEnumValueHandler.getName(it) }