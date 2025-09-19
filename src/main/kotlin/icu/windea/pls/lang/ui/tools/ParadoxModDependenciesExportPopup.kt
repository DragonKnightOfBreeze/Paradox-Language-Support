package icu.windea.pls.lang.ui.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.exporter.ParadoxModExporter
import javax.swing.Icon

class ParadoxModDependenciesExportPopup(
    private val project: Project,
    private val table: ParadoxModDependenciesTable
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

    override fun onChosen(selectedValue: ParadoxModExporter, finalChoice: Boolean) = doFinalStep {
        selectedValue.execute(project, table)
    }
}
