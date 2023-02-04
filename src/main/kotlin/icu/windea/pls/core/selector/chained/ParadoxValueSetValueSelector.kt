package icu.windea.pls.core.selector.chained

import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxValueSetValueSelector: ChainedParadoxSelector<ParadoxScriptString>()

fun valueSetValueSelector() = ParadoxValueSetValueSelector()

fun ParadoxValueSetValueSelector.declarationOnly() =
	filterBy { !ParadoxValueSetValueHandler.getRead(it) }

fun ParadoxValueSetValueSelector.distinctByValue() =
	distinctBy { ParadoxValueSetValueHandler.getName(it) }