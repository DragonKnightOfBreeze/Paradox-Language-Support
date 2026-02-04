package icu.windea.pls.extensions.diagram

import com.intellij.diagram.DiagramDataKeys
import com.intellij.diagram.DiagramDataModel
import com.intellij.diagram.DiagramNode
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import com.intellij.util.application
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.extensions.diagram.provider.ParadoxDiagramProvider
import icu.windea.pls.lang.ParadoxModificationTrackers

// com.intellij.uml.java.JavaUmlDataModel

abstract class ParadoxDiagramDataModel(
    project: Project,
    val file: VirtualFile?,
    open val provider: ParadoxDiagramProvider,
) : DiagramDataModel<PsiElement>(project, provider) {
    val gameType get() = provider.gameType

    // rootFile - PsiFileSystemItem
    val originalFile get() = file?.getUserData(DiagramDataKeys.ORIGINAL_ELEMENT) as? PsiFileSystemItem

    private val _nodes = mutableSetOf<ParadoxDiagramNode>()
    private val _edges = mutableSetOf<ParadoxDiagramEdge>()
    private val lock = Any()

    override fun getNodes() = _nodes

    override fun getEdges() = _edges

    override fun getNodeName(node: DiagramNode<PsiElement>) = node.tooltip.or.anonymous()

    override fun addElement(element: PsiElement?) = null

    override fun dispose() {
        cleanAllNodeAndEdges()
    }

    override fun refreshDataModel() {
        ProgressManager.checkCanceled()
        synchronized(lock) {
            cleanAllNodeAndEdges()
            if (application.isReadAccessAllowed) {
                updateDataModel()
            } else {
                ReadAction.nonBlocking<Unit> { updateDataModel() }
                    .inSmartMode(project)
                    .withDocumentsCommitted(project)
                    .expireWith(this)
                    .executeSynchronously()
            }
        }
    }

    private fun cleanAllNodeAndEdges() {
        nodes.clear()
        edges.clear()
    }

    protected abstract fun updateDataModel()

    override fun getModificationTracker(): ModificationTracker {
        return ParadoxModificationTrackers.ScriptFile
    }
}
