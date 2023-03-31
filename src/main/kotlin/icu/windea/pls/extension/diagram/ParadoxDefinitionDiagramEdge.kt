package icu.windea.pls.extension.diagram

import com.intellij.diagram.*

open class ParadoxDefinitionDiagramEdge(
    override val source: ParadoxDefinitionDiagramNode,
    override val target: ParadoxDefinitionDiagramNode,
    relationship: DiagramRelationshipInfo
) : ParadoxDiagramEdge(source, target, relationship)
