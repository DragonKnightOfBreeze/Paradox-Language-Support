package icu.windea.pls.core.selector.chained

import com.intellij.openapi.project.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariableSelector(project: Project): ChainedParadoxSelector<ParadoxScriptScriptedVariable>(project)

fun scriptedVariableSelector(project: Project) = ParadoxScriptedVariableSelector(project)

fun ParadoxScriptedVariableSelector.distinctByName() =
	distinctBy { it.name }