package icu.windea.pls.core.tool.exporter

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.ui.table.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.*
import javax.swing.*

interface ParadoxModDependenciesExporter {
    val icon: Icon? get() = null
    val text: String
    
    fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel)
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxModDependenciesExporter>("icu.windea.pls.paradoxModDependenciesExporter")
    }
}