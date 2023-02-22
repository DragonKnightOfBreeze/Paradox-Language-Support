package icu.windea.pls.core.tool.importer

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.ui.table.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.*
import javax.swing.*

interface ParadoxModDependenciesImporter {
    val icon: Icon? get() = null
    val text: String
    
    fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel)
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxModDependenciesImporter>("icu.windea.pls.paradoxModDependenciesImporter")
    }
}

