package icu.windea.pls.core.search.selectors.chained

import com.intellij.openapi.project.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariableSelector(project: Project, context: Any? = null): ChainedParadoxSelector<ParadoxScriptScriptedVariable>(project, context)

fun scriptedVariableSelector(project: Project, context: Any? = null) = ParadoxScriptedVariableSelector(project, context)

fun ParadoxScriptedVariableSelector.distinctByName() =
	distinctBy { it.name }