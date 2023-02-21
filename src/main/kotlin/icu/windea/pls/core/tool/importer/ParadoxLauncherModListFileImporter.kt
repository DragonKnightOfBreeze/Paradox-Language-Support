package icu.windea.pls.core.tool.importer

import com.intellij.openapi.project.*
import com.intellij.ui.table.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.*
import javax.swing.*

class ParadoxLauncherModListFileImporter : ParadoxModDependenciesImporter {
    override val icon: Icon
        get() = TODO("Not yet implemented")
    override val text: String
        get() = TODO("Not yet implemented")
    
    override fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel) {
        //TODO
    }
}