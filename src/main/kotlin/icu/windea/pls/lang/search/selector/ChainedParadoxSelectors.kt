package icu.windea.pls.lang.search.selector

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.model.index.ParadoxDefineIndexInfo
import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.model.index.ParadoxParameterIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ChainedParadoxSelectorHolder(
    val project: Project,
    val context: Any? = null
)

fun selector(project: Project, context: Any? = null) = ChainedParadoxSelectorHolder(project, context)

fun ChainedParadoxSelectorHolder.scriptedVariable() = ParadoxSearchSelector<ParadoxScriptScriptedVariable>(project, context)

fun ChainedParadoxSelectorHolder.definition() = ParadoxSearchSelector<ParadoxScriptDefinitionElement>(project, context)

fun ChainedParadoxSelectorHolder.localisation() = ParadoxSearchSelector<ParadoxLocalisationProperty>(project, context)

fun ChainedParadoxSelectorHolder.inlineScriptUsage() = ParadoxSearchSelector<ParadoxScriptProperty>(project, context)

fun ChainedParadoxSelectorHolder.definitionInjection() = ParadoxSearchSelector<ParadoxScriptProperty>(project, context)

fun ChainedParadoxSelectorHolder.file() = ParadoxSearchSelector<VirtualFile>(project, context)

fun ChainedParadoxSelectorHolder.complexEnumValue() = ParadoxSearchSelector<ParadoxComplexEnumValueIndexInfo>(project, context)

fun ChainedParadoxSelectorHolder.dynamicValue() = ParadoxSearchSelector<ParadoxDynamicValueIndexInfo>(project, context)

fun ChainedParadoxSelectorHolder.define() = ParadoxSearchSelector<ParadoxDefineIndexInfo>(project, context)

fun ChainedParadoxSelectorHolder.parameter() = ParadoxSearchSelector<ParadoxParameterIndexInfo>(project, context)

fun ChainedParadoxSelectorHolder.localisationParameter() = ParadoxSearchSelector<ParadoxLocalisationParameterIndexInfo>(project, context)
