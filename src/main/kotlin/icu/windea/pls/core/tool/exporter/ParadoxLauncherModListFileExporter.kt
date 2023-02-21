package icu.windea.pls.core.tool.exporter

import com.intellij.openapi.project.*
import com.intellij.ui.table.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.*
import javax.swing.*

class ParadoxLauncherModListFileExporter : ParadoxModDependenciesExporter {
    override val icon: Icon
        get() = TODO("Not yet implemented")
    override val text: String
        get() = TODO("Not yet implemented")
    
    override fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel) {
        //TODO
    }
}