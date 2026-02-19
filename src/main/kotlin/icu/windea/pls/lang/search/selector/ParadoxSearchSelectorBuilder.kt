package icu.windea.pls.lang.search.selector

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.index.ParadoxComplexEnumValueIndexInfo
import icu.windea.pls.model.index.ParadoxDefinitionIndexInfo
import icu.windea.pls.model.index.ParadoxDefinitionInjectionIndexInfo
import icu.windea.pls.model.index.ParadoxDynamicValueIndexInfo
import icu.windea.pls.model.index.ParadoxLocalisationParameterIndexInfo
import icu.windea.pls.model.index.ParadoxParameterIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxSearchSelectorBuilder(
    val project: Project,
    val context: Any? = null
) {
    fun scriptedVariable() = ParadoxSearchSelector<ParadoxScriptScriptedVariable>(project, context)

    fun define() = ParadoxSearchSelector<ParadoxScriptProperty>(project, context)

    fun inlineScriptUsage() = ParadoxSearchSelector<ParadoxScriptProperty>(project, context)

    fun localisation() = ParadoxSearchSelector<ParadoxLocalisationProperty>(project, context)

    fun file() = ParadoxSearchSelector<VirtualFile>(project, context)

    fun definition() = ParadoxSearchSelector<ParadoxDefinitionIndexInfo>(project, context)

    fun definitionInjection() = ParadoxSearchSelector<ParadoxDefinitionInjectionIndexInfo>(project, context)

    fun complexEnumValue() = ParadoxSearchSelector<ParadoxComplexEnumValueIndexInfo>(project, context)

    fun dynamicValue() = ParadoxSearchSelector<ParadoxDynamicValueIndexInfo>(project, context)

    fun parameter() = ParadoxSearchSelector<ParadoxParameterIndexInfo>(project, context)

    fun localisationParameter() = ParadoxSearchSelector<ParadoxLocalisationParameterIndexInfo>(project, context)
}

fun selector(project: Project, context: Any? = null) = ParadoxSearchSelectorBuilder(project, context)
