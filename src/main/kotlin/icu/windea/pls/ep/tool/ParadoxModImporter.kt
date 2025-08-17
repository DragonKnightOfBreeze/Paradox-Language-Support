package icu.windea.pls.ep.tool

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import icu.windea.pls.lang.ui.tools.*
import javax.swing.*

interface ParadoxModImporter {
    val icon: Icon? get() = null
    val text: String

    fun execute(project: Project, table: ParadoxModDependenciesTable)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModImporter>("icu.windea.pls.modImporter")
    }
}
