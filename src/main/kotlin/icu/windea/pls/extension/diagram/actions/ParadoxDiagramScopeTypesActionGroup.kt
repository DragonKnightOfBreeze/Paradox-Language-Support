package icu.windea.pls.extension.diagram.actions

import com.intellij.diagram.DiagramBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Toggleable
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.extension.diagram.ParadoxDiagramDataModel
import icu.windea.pls.extension.diagram.PlsDiagramBundle
import icu.windea.pls.extension.diagram.provider.ParadoxDiagramProvider
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeTypes

//com.intellij.uml.core.actions.scopes.UmlScopesActionGroup

class ParadoxDiagramScopeTypesActionGroup(
    val builder: DiagramBuilder
) : ActionGroup(PlsDiagramBundle.message("group.scopeTypes.name"), true), Toggleable, DumbAware {
    init {
        templatePresentation.icon = AllIcons.General.Filter
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val provider = builder.provider
        if (provider !is ParadoxDiagramProvider) return getDefaultChildren()
        val project = builder.project
        val context = (builder.dataModel as ParadoxDiagramDataModel).originalFile
        val scopeTypes = provider.getScopeTypes(project, context)
        if (scopeTypes.isNullOrEmpty()) return getDefaultChildren()
        return scopeTypes.mapToArray { ParadoxDiagramChangeScopeTypeAction(it, builder) }
    }

    private fun getDefaultChildren(): Array<AnAction> {
        return arrayOf(ParadoxDiagramChangeScopeTypeAction(ParadoxSearchScopeTypes.All, builder))
    }
}
