package icu.windea.pls.lang.ui.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.importer.ParadoxModImporter
import javax.swing.Icon

class ParadoxModDependenciesImportPopup(
    private val project: Project,
    private val table: ParadoxModDependenciesTable
) : BaseListPopupStep<ParadoxModImporter>(getTitle(), *getValues()) {
    companion object {
        private fun getTitle() = PlsBundle.message("mod.dependencies.toolbar.action.import.popup.title")

        private fun getValues() = ParadoxModImporter.EP_NAME.extensions
    }

    override fun getIconFor(value: ParadoxModImporter): Icon? {
        return value.icon
    }

    override fun getTextFor(value: ParadoxModImporter): String {
        return value.text
    }

    override fun isSpeedSearchEnabled(): Boolean {
        return true
    }

    override fun onChosen(selectedValue: ParadoxModImporter, finalChoice: Boolean) = doFinalStep {
        selectedValue.execute(project, table)
    }
}
