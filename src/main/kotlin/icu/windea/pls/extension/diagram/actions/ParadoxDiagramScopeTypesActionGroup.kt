package icu.windea.pls.extension.diagram.actions

import com.intellij.diagram.*
import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.scope.type.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*

//com.intellij.uml.core.actions.scopes.UmlScopesActionGroup

class ParadoxDiagramScopeTypesActionGroup(
    val builder: DiagramBuilder
): ActionGroup(PlsDiagramBundle.message("group.scopeTypes.name"), true), Toggleable, DumbAware {
    init {
        templatePresentation.icon = AllIcons.General.Filter
    }
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val provider = builder.provider
        if(provider !is ParadoxDiagramProvider) return getDefaultChildren()
        val project = builder.project
        val context = (builder.dataModel as ParadoxDiagramDataModel).originalFile
        val scopeTypes = provider.getScopeTypes(project, context)
        if(scopeTypes.isNullOrEmpty()) return getDefaultChildren()
        return scopeTypes.mapToArray { ParadoxDiagramChangeScopeTypeAction(it, builder) }
    }
    
    private fun getDefaultChildren(): Array<AnAction> {
        return arrayOf(ParadoxDiagramChangeScopeTypeAction(ParadoxSearchScopeTypes.All, builder))
    }
}