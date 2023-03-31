package icu.windea.pls.extension.diagram.extras

import com.intellij.diagram.settings.*
import com.intellij.openapi.graph.*
import com.intellij.openapi.graph.layout.*
import com.intellij.openapi.graph.settings.*
import com.intellij.openapi.project.*
import icu.windea.pls.extension.diagram.provider.*

abstract class ParadoxDiagramExtras(
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
}