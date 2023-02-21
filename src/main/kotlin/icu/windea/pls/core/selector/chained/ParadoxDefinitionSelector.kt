package icu.windea.pls.core.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionSelector(project: Project) : ChainedParadoxSelector<ParadoxScriptDefinitionElement>(project)

fun definitionSelector(project: Project) = ParadoxDefinitionSelector(project)

fun ParadoxDefinitionSelector.distinctByName() =
    distinctBy { ParadoxDefinitionHandler.getName(it) }