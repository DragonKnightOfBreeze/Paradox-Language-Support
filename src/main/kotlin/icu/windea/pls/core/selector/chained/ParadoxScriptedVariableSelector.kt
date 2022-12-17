package icu.windea.pls.core.selector.chained

import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariableSelector: ChainedParadoxSelector<ParadoxScriptScriptedVariable>()

fun scriptedVariableSelector() = ParadoxScriptedVariableSelector()

fun ParadoxScriptedVariableSelector.distinctByName() =
	distinctBy { it.name }