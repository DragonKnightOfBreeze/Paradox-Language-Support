package icu.windea.pls.core.tool.importer

import com.fasterxml.jackson.module.kotlin.*
import com.intellij.notification.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.*
import icu.windea.pls.lang.model.*

class ParadoxFromLauncherImporter : ParadoxModDependenciesImporter {
    val defaultSelected: VirtualFile? = null
    
    override val text: String = PlsBundle.message("mod.importer.launcher")
    
    override fun execute(project: Project, tableView: TableView<ParadoxModDependencySettingsState>, tableModel: ParadoxModDependenciesTableModel) {
        
    }
}