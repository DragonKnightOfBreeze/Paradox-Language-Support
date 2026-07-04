package icu.windea.pls.extensions.diagram.actions

import com.intellij.diagram.DiagramBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Toggleable
import com.intellij.openapi.project.DumbAware
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.extensions.diagram.ChronicleDiagramBundle
import icu.windea.pls.extensions.diagram.ParadoxDiagramDataModel
import icu.windea.pls.extensions.diagram.provider.ParadoxDiagramProvider
import icu.windea.pls.lang.search.scope.ParadoxSearchScopeTypes

// com.intellij.uml.core.actions.scopes.UmlScopesActionGroup

class ParadoxDiagramScopeTypesActionGroup(
    val builder: DiagramBuilder
) : ActionGroup(ChronicleDiagramBundle.message("group.scopeTypes.name"), true), Toggleable, DumbAware {
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
        val children = scopeTypes.map { ParadoxDiagramChangeScopeTypeAction(it, builder) }
        return children.toTypedArray()
    }

    private fun getDefaultChildren(): Array<AnAction> {
        return arrayOf(ParadoxDiagramChangeScopeTypeAction(ParadoxSearchScopeTypes.All, builder))
    }
}
