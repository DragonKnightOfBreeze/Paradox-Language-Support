package icu.windea.pls.core.search.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

typealias ParadoxComplexEnumValueSelector = ChainedParadoxSelector<ParadoxComplexEnumValueInfo>

fun complexEnumValueSelector(project: Project, context: Any? = null) = ParadoxComplexEnumValueSelector(project, context)

fun ParadoxComplexEnumValueSelector.declarationOnly() = filterBy { ParadoxComplexEnumValueHandler.isDeclaration(it) }

fun ParadoxComplexEnumValueSelector.distinctByName() = distinctBy { it.name }
