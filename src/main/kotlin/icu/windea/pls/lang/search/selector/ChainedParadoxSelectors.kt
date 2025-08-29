package icu.windea.pls.lang.search.selector

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.indexInfo.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.model.indexInfo.ParadoxDefineIndexInfo
import icu.windea.pls.model.indexInfo.ParadoxDynamicValueIndexInfo
import icu.windea.pls.model.indexInfo.ParadoxInlineScriptUsageIndexInfo
import icu.windea.pls.model.indexInfo.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.model.indexInfo.ParadoxParameterIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ChainedParadoxSelectorHolder(
    val project: Project,
    val context: Any? = null
)

fun selector(project: Project, context: Any? = null) = ChainedParadoxSelectorHolder(project, context)

fun ChainedParadoxSelectorHolder.scriptedVariable() = ChainedParadoxSelector<ParadoxScriptScriptedVariable>(project, context)

fun ChainedParadoxSelectorHolder.definition() = ChainedParadoxSelector<ParadoxScriptDefinitionElement>(project, context)

fun ChainedParadoxSelectorHolder.localisation() = ChainedParadoxSelector<ParadoxLocalisationProperty>(project, context)

fun ChainedParadoxSelectorHolder.file() = ChainedParadoxSelector<VirtualFile>(project, context)

fun ChainedParadoxSelectorHolder.complexEnumValue() = ChainedParadoxSelector<ParadoxComplexEnumValueIndexInfo>(project, context)

fun ChainedParadoxSelectorHolder.dynamicValue() = ChainedParadoxSelector<ParadoxDynamicValueIndexInfo>(project, context)

fun ChainedParadoxSelectorHolder.define() = ChainedParadoxSelector<ParadoxDefineIndexInfo>(project, context)

fun ChainedParadoxSelectorHolder.inlineScriptUsage() = ChainedParadoxSelector<ParadoxInlineScriptUsageIndexInfo.Compact>(project, context)

fun ChainedParadoxSelectorHolder.parameter() = ChainedParadoxSelector<ParadoxParameterIndexInfo>(project, context)

fun ChainedParadoxSelectorHolder.localisationParameter() = ChainedParadoxSelector<ParadoxLocalisationParameterIndexInfo>(project, context)
