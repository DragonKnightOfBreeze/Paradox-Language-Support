package icu.windea.pls.extension.diagram

import com.intellij.diagram.DiagramEdgeBase
import com.intellij.diagram.DiagramRelationshipInfo
import com.intellij.psi.PsiElement

open class ParadoxDiagramEdge(
    open val source: ParadoxDiagramNode,
    open val target: ParadoxDiagramNode,
    relationship: DiagramRelationshipInfo
) : DiagramEdgeBase<PsiElement>(source, target, relationship)
