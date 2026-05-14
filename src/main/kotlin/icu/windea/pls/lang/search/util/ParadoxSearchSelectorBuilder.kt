package icu.windea.pls.lang.search.util

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
    fun define() = ParadoxSearchSelector<ParadoxScriptProperty>(project, context)
}

fun selector(project: Project, context: Any? = null) = ParadoxSearchSelectorBuilder(project, context)
