package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.extension.diagram.provider.*

abstract class ParadoxDiagramDataModel(
    project: Project,
    val file: VirtualFile?,
    val provider: ParadoxDiagramProvider,
): DiagramDataModel<PsiElement>(project, provider)  {
    val gameType get() = provider.gameType
    
    val originalFile get() = file?.getUserData(DiagramDataKeys.ORIGINAL_ELEMENT) as? VirtualFile?
    
    private val _nodes = mutableSetOf<ParadoxDiagramNode>()
    private val _edges = mutableSetOf<ParadoxDiagramEdge>()
    
    override fun getNodes() = _nodes
    
    override fun getEdges() = _edges
    
    override fun getNodeName(node: DiagramNode<PsiElement>) = node.tooltip.orAnonymous()
    
    override fun addElement(element: PsiElement?) = null
    
    override fun dispose() {
        
    }
}