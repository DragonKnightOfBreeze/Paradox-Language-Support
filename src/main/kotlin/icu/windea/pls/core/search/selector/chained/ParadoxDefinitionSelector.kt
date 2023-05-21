package icu.windea.pls.core.search.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

typealias ParadoxDefinitionSelector = ChainedParadoxSelector<ParadoxScriptDefinitionElement>

fun definitionSelector(project: Project, context: Any? = null) = ParadoxDefinitionSelector(project, context)

fun ParadoxDefinitionSelector.distinctByName() = distinctBy { ParadoxDefinitionHandler.getName(it) }