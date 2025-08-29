package icu.windea.pls.extension.diagram.actions

import com.intellij.diagram.DiagramDataKeys
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.extension.diagram.provider.ParadoxDiagramProvider
import icu.windea.pls.extension.diagram.settings.PlsDiagramSettingsConfigurable

//com.intellij.uml.core.actions.DiagramOpenSettingsAction

class ParadoxDiagramOpenSettingsAction : AnAction(), DumbAware {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

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
        ShowSettingsUtil.getInstance().showSettingsDialog(project, PlsDiagramSettingsConfigurable::class.java)
    }
}

