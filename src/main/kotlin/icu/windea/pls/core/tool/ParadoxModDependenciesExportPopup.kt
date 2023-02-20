package icu.windea.pls.core.tool

import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import com.intellij.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.core.tool.exporter.*
import javax.swing.*

private fun getTitle() = PlsBundle.message("mod.dependencies.toolbar.action.export.popup.title")

private fun getValues() = ParadoxModDependenciesExporter.EP_NAME.extensions

class ParadoxModDependenciesExportPopup(
    private val project: Project,
    private val tableView: TableView<ParadoxModDependencySettingsState>,
    private val tableModel: ParadoxModDependenciesTableModel
) : BaseListPopupStep<ParadoxModDependenciesExporter>(getTitle(), *getValues()) {
    override fun getIconFor(value: ParadoxModDependenciesExporter): Icon? {
        return value.icon
    }
    
    override fun getTextFor(value: ParadoxModDependenciesExporter): String {
        return value.text
    }
    
    override fun isSpeedSearchEnabled(): Boolean {
        return true
    }
    
    override fun onChosen(selectedValue: ParadoxModDependenciesExporter, finalChoice: Boolean): PopupStep<*> {
        return doFinalStep { selectedValue.execute(project, tableView, tableModel) }
    }
}
