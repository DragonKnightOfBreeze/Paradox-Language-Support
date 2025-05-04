package icu.windea.pls.lang.hierarchy.type

import com.intellij.icons.*
import com.intellij.ide.hierarchy.*
import com.intellij.openapi.actionSystem.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import javax.swing.*

interface ParadoxDefinitionHierarchyActions {
    sealed class ViewActionBase(
        text: String,
        description: String,
        icon: Icon,
        val type: ParadoxDefinitionHierarchyType
    ) : ChangeHierarchyViewActionBase(text, description, icon) {
        override fun getTypeName() = type.text

        override fun update(event: AnActionEvent) {
            super.update(event)
            val hierarchyBrowser = getHierarchyBrowser(event.dataContext)
            val definitionInfo = hierarchyBrowser?.castOrNull<ParadoxDefinitionHierarchyBrowser>()
                ?.element?.castOrNull<ParadoxScriptDefinitionElement>()
                ?.definitionInfo
            val visible = definitionInfo != null && type.predicate(definitionInfo)
            event.presentation.isVisible = visible
        }

        override fun actionPerformed(event: AnActionEvent) {
            super.actionPerformed(event)
            val hierarchyBrowser = getHierarchyBrowser(event.dataContext)
            if (hierarchyBrowser !is ParadoxDefinitionHierarchyBrowser) return
            hierarchyBrowser.type = type
        }
    }

    class ViewDefinitionHierarchyAction : ViewActionBase(
        PlsBundle.message("action.view.hierarchy.definition"),
        PlsBundle.message("action.view.hierarchy.definition.description"),
        AllIcons.Hierarchy.Class,
        ParadoxDefinitionHierarchyType.Type
    )

    class ViewDefinitionHierarchyWithSubtypesAction : ViewActionBase(
        PlsBundle.message("action.view.hierarchy.definition.with.subtypes"),
        PlsBundle.message("action.view.hierarchy.definition.with.subtypes.description"),
        AllIcons.Hierarchy.Class,
        ParadoxDefinitionHierarchyType.TypeAndSubtypes
    )

    class ViewEventTreeInvokerAction : ViewActionBase(
        PlsBundle.message("action.view.hierarchy.eventTree.invoker"),
        PlsBundle.message("action.view.hierarchy.eventTree.invoker.description"),
        AllIcons.Hierarchy.Supertypes,
        ParadoxDefinitionHierarchyType.EventTreeInvoker
    )

    class ViewEventTreeInvokedAction : ViewActionBase(
        PlsBundle.message("action.view.hierarchy.eventTree.invoked"),
        PlsBundle.message("action.view.hierarchy.eventTree.invoked.description"),
        AllIcons.Hierarchy.Subtypes,
        ParadoxDefinitionHierarchyType.EventTreeInvoked
    )

    class ViewTechTreePreAction : ViewActionBase(
        PlsBundle.message("action.view.hierarchy.techTree.pre"),
        PlsBundle.message("action.view.hierarchy.techTree.pre.description"),
        AllIcons.Hierarchy.Supertypes,
        ParadoxDefinitionHierarchyType.TechTreePre
    )

    class ViewTechTreePostAction : ViewActionBase(
        PlsBundle.message("action.view.hierarchy.techTree.post"),
        PlsBundle.message("action.view.hierarchy.techTree.post.description"),
        AllIcons.Hierarchy.Subtypes,
        ParadoxDefinitionHierarchyType.TechTreePost
    )
}
