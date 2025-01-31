package icu.windea.pls.tools.importer

import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.ui.table.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.tools.ui.*
import javax.swing.*

interface ParadoxModImporter {
    val icon: Icon? get() = null
    val text: String

    fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel)

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxModImporter>("icu.windea.pls.modImporter")
    }
}

