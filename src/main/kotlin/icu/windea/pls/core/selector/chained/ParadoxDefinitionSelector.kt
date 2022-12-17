package icu.windea.pls.core.selector.chained

import icu.windea.pls.core.handler.*
import icu.windea.pls.core.psi.*

class ParadoxDefinitionSelector: ChainedParadoxSelector<ParadoxDefinitionProperty>()

fun definitionSelector() = ParadoxDefinitionSelector()

fun ParadoxDefinitionSelector.distinctByName() =
	distinctBy { ParadoxDefinitionHandler.getName(it) }