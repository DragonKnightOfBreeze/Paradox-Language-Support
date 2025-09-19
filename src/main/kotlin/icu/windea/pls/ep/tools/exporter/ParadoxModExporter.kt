package icu.windea.pls.ep.tools.exporter

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import icu.windea.pls.lang.ui.tools.ParadoxModDependenciesTable
import javax.swing.Icon

interface ParadoxModExporter {
    val icon: Icon? get() = null
    val text: String

    fun execute(project: Project, table: ParadoxModDependenciesTable)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModExporter>("icu.windea.pls.modExporter")
    }
}
