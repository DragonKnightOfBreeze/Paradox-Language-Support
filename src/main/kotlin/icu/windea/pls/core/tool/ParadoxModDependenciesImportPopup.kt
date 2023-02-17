package icu.windea.pls.core.tool

import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.ui.popup.util.*
import icu.windea.pls.*
import icu.windea.pls.core.tool.dependencies.*
import javax.swing.*

private fun getTitle() = PlsBundle.message("mod.dependencies.toolbar.action.import.popup.title")

private fun getValues() = ParadoxModDependenciesImporter.EP_NAME.extensions

class ParadoxModDependenciesImportPopup : BaseListPopupStep<ParadoxModDependenciesImporter>(getTitle(), *getValues()) {
    override fun getIconFor(value: ParadoxModDependenciesImporter): Icon? {
        return value.icon
    }
    
    override fun getTextFor(value: ParadoxModDependenciesImporter): String {
        return value.text
    }
    
    override fun isSpeedSearchEnabled(): Boolean {
        return true
    }
    
    override fun onChosen(selectedValue: ParadoxModDependenciesImporter, finalChoice: Boolean): PopupStep<*> {
        return doFinalStep { selectedValue.execute() }
    }
}
