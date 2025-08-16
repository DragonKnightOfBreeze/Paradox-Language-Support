package icu.windea.pls.extension.diagram.actions

import com.intellij.diagram.*
import com.intellij.diagram.util.*
import com.intellij.openapi.actionSystem.*
import com.intellij.uml.core.actions.visibility.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.lang.search.scope.type.*
import javax.swing.*

//com.intellij.uml.core.actions.scopes.UmlChangeScopeAction

class ParadoxDiagramChangeScopeTypeAction(
    val scopeType: ParadoxSearchScopeType,
    val builder: DiagramBuilder
) : DiagramAction(scopeType.text, null, null) {
    object Icons {
        val SELECTED_ICON = SelectedVisibilityIcon()
        val DESELECTED_ICON = DeselectedVisibilityIcon()
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun getActionName(): String {
        return PlsDiagramBundle.message("action.changeScopeType.name", scopeType.text)
    }

    private fun getActionIcon(): Icon {
        val project = builder.project
        val provider = builder.provider
        if (provider !is ParadoxDiagramProvider) return Icons.DESELECTED_ICON
        val settings = provider.getDiagramSettings(project)?.state ?: return Icons.DESELECTED_ICON
        val currentScopeType = settings.scopeType
        val selected = ParadoxSearchScopeTypes.get(currentScopeType).id == scopeType.id
        return if (selected) Icons.SELECTED_ICON else Icons.DESELECTED_ICON
    }

    override fun update(e: AnActionEvent) {
        val builder = e.getData(DiagramDataKeys.BUILDER)
        val provider = builder?.provider
        e.presentation.isEnabledAndVisible = provider is ParadoxDiagramProvider
        e.presentation.icon = getActionIcon()
    }

    override fun perform(e: AnActionEvent) {
        val project = builder.project
        val provider = builder.provider
        if (provider !is ParadoxDiagramProvider) return
        val settings = provider.getDiagramSettings(project)?.state ?: return
        settings.scopeType = scopeType.id
        DiagramUpdateService.getInstance().requestDataModelRefreshPreservingLayout(builder).runAsync()
    }
}
