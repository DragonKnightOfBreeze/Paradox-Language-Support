package icu.windea.pls.core.search.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.script.psi.*

typealias ParadoxScriptedVariableSelector = ChainedParadoxSelector<ParadoxScriptScriptedVariable>

fun scriptedVariableSelector(project: Project, context: Any? = null) = ParadoxScriptedVariableSelector(project, context)

fun ParadoxScriptedVariableSelector.distinctByName() =
	distinctBy { it.name }