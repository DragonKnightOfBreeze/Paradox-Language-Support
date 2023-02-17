package icu.windea.pls.core.tool

import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import icu.windea.pls.*
import icu.windea.pls.core.tool.dependencies.*
import javax.swing.*

private fun getTitle() = PlsBundle.message("mod.dependencies.toolbar.action.export.popup.title")

private fun getValues() = ParadoxModDependenciesExporter.EP_NAME.extensions

class ParadoxModDependenciesExportPopup : BaseListPopupStep<ParadoxModDependenciesExporter>(getTitle(), *getValues()) {
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
        return doFinalStep { selectedValue.execute() }
    }
}
