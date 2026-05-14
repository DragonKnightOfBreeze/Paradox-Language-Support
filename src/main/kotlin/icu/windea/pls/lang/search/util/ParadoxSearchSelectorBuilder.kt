package icu.windea.pls.lang.search.util

import com.intellij.openapi.project.Project
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxSearchSelectorBuilder(
    val project: Project,
    val context: Any? = null
) {
    fun define() = ParadoxSearchSelector<ParadoxScriptProperty>(project, context)
}

fun selector(project: Project, context: Any? = null) = ParadoxSearchSelectorBuilder(project, context)
