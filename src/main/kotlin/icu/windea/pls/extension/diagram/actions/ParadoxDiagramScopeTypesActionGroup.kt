package icu.windea.pls.extension.diagram.actions

import com.intellij.diagram.DiagramBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.*

//com.intellij.uml.core.actions.scopes.UmlScopesActionGroup

class ParadoxDiagramScopeTypesActionGroup(
    val builder: DiagramBuilder
): ActionGroup(PlsDiagramBundle.message("group.scopeTypes.name"), true), Toggleable, DumbAware {
    init {
        templatePresentation.icon = AllIcons.General.Filter
    }
    
    override fun update(e: AnActionEvent) {
        val provider = builder.provider
        if(provider !is ParadoxDiagramProvider) return
        val scopeTypes = provider.getScopeTypes()
        e.presentation.isEnabledAndVisible = !scopeTypes.isNullOrEmpty()
    }
    
    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        val provider = builder.provider
        if(provider !is ParadoxDiagramProvider) return AnAction.EMPTY_ARRAY
        val scopeTypes = provider.getScopeTypes()
        if(scopeTypes.isNullOrEmpty()) return AnAction.EMPTY_ARRAY
        return scopeTypes.mapToArray { ParadoxDiagramChangeScopeTypeAction(it, builder) }
    }
}