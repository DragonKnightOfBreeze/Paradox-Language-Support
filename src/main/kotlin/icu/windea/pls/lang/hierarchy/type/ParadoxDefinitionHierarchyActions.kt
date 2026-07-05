package icu.windea.pls.lang.hierarchy.type

import com.intellij.icons.AllIcons
import com.intellij.ide.hierarchy.ChangeHierarchyViewActionBase
import com.intellij.openapi.actionSystem.AnActionEvent
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import javax.swing.Icon

interface ParadoxDefinitionHierarchyActions {
    sealed class ViewActionBase(
        text: String,
        description: String,
        icon: Icon,
        val type: ParadoxDefinitionHierarchyType
    ) : ChangeHierarchyViewActionBase(text, description, icon) {
        override fun getTypeName() = type.text

        override fun update(e: AnActionEvent) {
            super.update(e)
            val hierarchyBrowser = getHierarchyBrowser(e.dataContext)
            val definitionInfo = hierarchyBrowser?.castOrNull<ParadoxDefinitionHierarchyBrowser>()
                ?.element?.castOrNull<ParadoxDefinitionElement>()
                ?.definitionInfo
            val visible = definitionInfo != null && type.predicate(definitionInfo)
            e.presentation.isVisible = visible
        }

        override fun actionPerformed(event: AnActionEvent) {
            super.actionPerformed(event)
            val hierarchyBrowser = getHierarchyBrowser(event.dataContext)
            if (hierarchyBrowser !is ParadoxDefinitionHierarchyBrowser) return
            hierarchyBrowser.type = type
        }
    }

    class ViewDefinitionHierarchyAction : ViewActionBase(
        ChronicleBundle.message("action.view.hierarchy.definition"),
        ChronicleBundle.message("action.view.hierarchy.definition.description"),
        AllIcons.Hierarchy.Class,
        ParadoxDefinitionHierarchyType.Type
    )

    class ViewDefinitionHierarchyWithSubtypesAction : ViewActionBase(
        ChronicleBundle.message("action.view.hierarchy.definition.with.subtypes"),
        ChronicleBundle.message("action.view.hierarchy.definition.with.subtypes.description"),
        AllIcons.Hierarchy.Class,
        ParadoxDefinitionHierarchyType.TypeAndSubtypes
    )

    class ViewEventTreeInvokerAction : ViewActionBase(
        ChronicleBundle.message("action.view.hierarchy.eventTree.invoker"),
        ChronicleBundle.message("action.view.hierarchy.eventTree.invoker.description"),
        AllIcons.Hierarchy.Supertypes,
        ParadoxDefinitionHierarchyType.EventTreeInvoker
    )

    class ViewEventTreeInvokedAction : ViewActionBase(
        ChronicleBundle.message("action.view.hierarchy.eventTree.invoked"),
        ChronicleBundle.message("action.view.hierarchy.eventTree.invoked.description"),
        AllIcons.Hierarchy.Subtypes,
        ParadoxDefinitionHierarchyType.EventTreeInvoked
    )

    class ViewTechTreePreAction : ViewActionBase(
        ChronicleBundle.message("action.view.hierarchy.techTree.pre"),
        ChronicleBundle.message("action.view.hierarchy.techTree.pre.description"),
        AllIcons.Hierarchy.Supertypes,
        ParadoxDefinitionHierarchyType.TechTreePre
    )

    class ViewTechTreePostAction : ViewActionBase(
        ChronicleBundle.message("action.view.hierarchy.techTree.post"),
        ChronicleBundle.message("action.view.hierarchy.techTree.post.description"),
        AllIcons.Hierarchy.Subtypes,
        ParadoxDefinitionHierarchyType.TechTreePost
    )
}
