package icu.windea.pls.extension.diagram.extras

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.actions.DiagramToolbarActionsProvider
import com.intellij.diagram.settings.DiagramConfigGroup
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.SeparatorAction
import com.intellij.openapi.graph.GraphManager
import com.intellij.openapi.graph.layout.LayoutOrientation
import com.intellij.openapi.graph.layout.Layouter
import com.intellij.openapi.graph.settings.GraphSettings
import com.intellij.openapi.project.Project
import icu.windea.pls.core.children
import icu.windea.pls.extension.diagram.actions.ParadoxDiagramScopeTypesActionGroup
import icu.windea.pls.extension.diagram.provider.ParadoxDiagramProvider

open class ParadoxDiagramExtras(
    val provider: ParadoxDiagramProvider
) : DiagramExtrasEx() {
    override fun getCustomLayouter(settings: GraphSettings, project: Project?): Layouter {
        val layouter = GraphManager.getGraphManager().createHierarchicGroupLayouter()
        layouter.orientationLayouter = GraphManager.getGraphManager().createOrientationLayouter(LayoutOrientation.LEFT_TO_RIGHT)
        layouter.layerer = GraphManager.getGraphManager().createBFSLayerer()
        layouter.minimalNodeDistance = 20.0
        layouter.minimalEdgeDistance = 40.0
        return layouter
    }

    override fun getAdditionalDiagramSettings(): Array<out DiagramConfigGroup> {
        return provider.getAdditionalDiagramSettings()
    }

    override fun getToolbarActionsProvider(): DiagramToolbarActionsProvider {
        return object : DiagramToolbarActionsProvider by super.getToolbarActionsProvider() {
            override fun createToolbarActions(builder: DiagramBuilder): DefaultActionGroup {
                val actionGroup = super.createToolbarActions(builder)
                //before first separator
                val children = actionGroup.children
                val separatorIndex = children.indexOfFirst { it is SeparatorAction }
                val index = if (separatorIndex == -1) children.size else separatorIndex
                children.add(index, ParadoxDiagramScopeTypesActionGroup(builder))
                return actionGroup
            }

            override fun updateAllDiagramEditorToolbars(project: Project) {
                super.updateAllDiagramEditorToolbars(project)
            }
        }
    }
}
