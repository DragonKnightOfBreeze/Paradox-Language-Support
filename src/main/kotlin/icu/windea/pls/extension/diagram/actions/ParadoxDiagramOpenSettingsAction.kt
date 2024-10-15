package icu.windea.pls.extension.diagram.actions

import com.intellij.diagram.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.extension.diagram.settings.*

//com.intellij.uml.core.actions.DiagramOpenSettingsAction

class ParadoxDiagramOpenSettingsAction : AnAction(PlsDiagramBundle.message("action.openSettings.name"), null, null), DumbAware {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        val builder = e.getData(DiagramDataKeys.BUILDER) ?: return
        val provider = builder.provider
        e.presentation.isEnabledAndVisible = provider is ParadoxDiagramProvider
    }

    override fun actionPerformed(e: AnActionEvent) {
        val builder = e.getData(DiagramDataKeys.BUILDER) ?: return
        val project = builder.project
        val provider = builder.provider
        if (provider !is ParadoxDiagramProvider) return
        val settings = provider.getDiagramSettings(project) ?: return
        ShowSettingsUtil.getInstance().showSettingsDialog(project, ParadoxDiagramSettingsConfigurable::class.java)
    }
}

