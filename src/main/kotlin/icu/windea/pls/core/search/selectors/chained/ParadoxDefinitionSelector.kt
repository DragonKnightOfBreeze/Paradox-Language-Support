package icu.windea.pls.core.search.selectors.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionSelector(project: Project, context: Any? = null) : ChainedParadoxSelector<ParadoxScriptDefinitionElement>(project, context)

fun definitionSelector(project: Project, context: Any? = null) = ParadoxDefinitionSelector(project, context)

fun ParadoxDefinitionSelector.distinctByName() =
    distinctBy { ParadoxDefinitionHandler.getName(it) }