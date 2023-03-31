package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.psi.*

open class ParadoxDiagramEdge(
    open val source: ParadoxDiagramNode,
    open val target: ParadoxDiagramNode,
    relationship: DiagramRelationshipInfo
) : DiagramEdgeBase<PsiElement>(source, target, relationship)
