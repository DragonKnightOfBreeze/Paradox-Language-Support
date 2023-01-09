package icu.windea.pls.core.selector.chained

import icu.windea.pls.config.core.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionSelector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>()

fun definitionSelector() = ParadoxDefinitionSelector()

fun ParadoxDefinitionSelector.distinctByName() =
	distinctBy { ParadoxDefinitionHandler.getName(it) }