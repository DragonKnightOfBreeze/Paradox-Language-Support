package icu.windea.pls.tools.ui

import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import icu.windea.pls.*
import icu.windea.pls.tools.importer.*
import javax.swing.*

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

    override fun onChosen(selectedValue: ParadoxModImporter, finalChoice: Boolean): PopupStep<*>? {
        return doFinalStep { selectedValue.execute(project, table) }
    }
}
