package icu.windea.pls.core.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxValueSetValueSelector(project: Project): ChainedParadoxSelector<ParadoxScriptString>(project)

fun valueSetValueSelector(project: Project) = ParadoxValueSetValueSelector(project)

fun ParadoxValueSetValueSelector.declarationOnly() =
	filterBy { !ParadoxValueSetValueHandler.getRead(it) }

fun ParadoxValueSetValueSelector.distinctByValue() =
	distinctBy { ParadoxValueSetValueHandler.getName(it) }