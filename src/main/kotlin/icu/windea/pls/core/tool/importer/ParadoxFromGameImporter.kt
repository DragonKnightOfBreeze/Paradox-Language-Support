package icu.windea.pls.core.tool.importer

import com.intellij.openapi.project.*
import com.intellij.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.*

class ParadoxFromGameImporter : ParadoxModDependenciesImporter {
    override val text: String = PlsBundle.message("mod.importer.game")
    
    override fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel) {
        //TODO
    }
}