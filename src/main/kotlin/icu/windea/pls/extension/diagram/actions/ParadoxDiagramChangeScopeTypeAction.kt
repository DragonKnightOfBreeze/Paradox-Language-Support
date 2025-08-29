package icu.windea.pls.extension.diagram.actions

import com.intellij.diagram.DiagramAction
import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.util.DiagramUpdateService
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.uml.core.actions.visibility.DeselectedVisibilityIcon
import com.intellij.uml.core.actions.visibility.SelectedVisibilityIcon
import icu.windea.pls.core.orNull
import icu.windea.pls.extension.diagram.ParadoxDiagramDataModel
import icu.windea.pls.extension.diagram.PlsDiagramBundle
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeType
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeTypes
import javax.swing.Icon

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
        val dataModel = builder.dataModel
        if (dataModel !is ParadoxDiagramDataModel) return Icons.DESELECTED_ICON
        val originalFile = dataModel.originalFile
        val currentScopeType = dataModel.provider.getDiagramSettings(project)?.state?.scopeType?.orNull()
        val finalCurrentSearchScopeType = when {
            currentScopeType == ParadoxSearchScopeTypes.File.id && originalFile?.language !is ParadoxBaseLanguage -> null
            currentScopeType != null -> currentScopeType
            originalFile?.language is ParadoxBaseLanguage -> ParadoxSearchScopeTypes.File.id
            else -> null
        }
        val selected = ParadoxSearchScopeTypes.get(finalCurrentSearchScopeType).id == scopeType.id
        return if (selected) Icons.SELECTED_ICON else Icons.DESELECTED_ICON
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val dataModel = this.builder.dataModel
        if (dataModel !is ParadoxDiagramDataModel) return
        e.presentation.isEnabledAndVisible = true
        e.presentation.icon = getActionIcon()
    }

    override fun perform(e: AnActionEvent) {
        val project = builder.project
        val dataModel = builder.dataModel
        if (dataModel !is ParadoxDiagramDataModel) return
        val settings = dataModel.provider.getDiagramSettings(project)?.state ?: return
        settings.scopeType = scopeType.id
        DiagramUpdateService.getInstance().requestDataModelRefreshPreservingLayout(builder).runAsync()
    }
}
