package icu.windea.pls.extension.diagram.actions

import com.intellij.diagram.DiagramAction
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*

class ParadoxDiagramOpenSettingsAction: DiagramAction() {
    override fun getActionName(): String {
        return PlsDiagramBundle.message("action.openSettings.name")
    }
    
    override fun update(e: AnActionEvent) {
        super.update(e)
    }
    
    override fun perform(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(getEventProject(e), ParadoxDiagramSettingsConfigurable::class.java)
    }
}