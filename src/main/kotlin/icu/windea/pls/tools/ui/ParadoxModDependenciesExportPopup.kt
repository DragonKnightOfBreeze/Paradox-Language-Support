package icu.windea.pls.tools.ui

import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import icu.windea.pls.*
import icu.windea.pls.tools.exporter.*
import javax.swing.*

class ParadoxModDependenciesExportPopup(
    private val project: Project,
    private val table: ParadoxModDependenciesTable,
    private val tableModel: ParadoxModDependenciesTableModel
) : BaseListPopupStep<ParadoxModExporter>(getTitle(), *getValues()) {
    companion object {
        private fun getTitle() = PlsBundle.message("mod.dependencies.toolbar.action.export.popup.title")

        private fun getValues() = ParadoxModExporter.EP_NAME.extensions
    }

    override fun getIconFor(value: ParadoxModExporter): Icon? {
        return value.icon
    }

    override fun getTextFor(value: ParadoxModExporter): String {
        return value.text
    }

    override fun isSpeedSearchEnabled(): Boolean {
        return true
    }

    override fun onChosen(selectedValue: ParadoxModExporter, finalChoice: Boolean): PopupStep<*>? {
        return doFinalStep { selectedValue.execute(project, table, tableModel) }
    }
}
