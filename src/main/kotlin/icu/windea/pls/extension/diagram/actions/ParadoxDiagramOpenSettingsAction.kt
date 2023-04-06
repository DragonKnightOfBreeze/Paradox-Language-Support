package icu.windea.pls.extension.diagram.actions

import com.intellij.diagram.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.options.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.extension.diagram.settings.*

//com.intellij.uml.core.actions.DiagramOpenSettingsAction

class ParadoxDiagramOpenSettingsAction: DiagramAction(PlsDiagramBundle.message("action.openSettings.name"), null, null) {
    override fun getActionName(): String {
        return PlsDiagramBundle.message("action.openSettings.name")
    }
    
    override fun update(e: AnActionEvent) {
        val builder = e.getData(DiagramDataKeys.BUILDER)
        val provider = builder?.provider
        e.presentation.isEnabledAndVisible = provider is ParadoxDiagramProvider
    }
    
    override fun perform(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(getEventProject(e), ParadoxDiagramSettingsConfigurable::class.java)
    }
}

