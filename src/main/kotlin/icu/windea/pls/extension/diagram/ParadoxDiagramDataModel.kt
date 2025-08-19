package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import com.intellij.util.containers.ContainerUtil
import icu.windea.pls.core.util.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

//com.intellij.uml.java.JavaUmlDataModel

abstract class ParadoxDiagramDataModel(
    project: Project,
    val file: VirtualFile?,
    val provider: ParadoxDiagramProvider,
) : DiagramDataModel<PsiElement>(project, provider) {
    val gameType get() = provider.gameType

    //rootFile - PsiFileSystemItem
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
                ReadAction.nonBlocking(Callable { updateDataModel() })
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

    protected abstract fun showNode(definition: ParadoxScriptDefinitionElement, settings: ParadoxDiagramSettings.State): Boolean

    override fun getModificationTracker(): ModificationTracker {
        return ParadoxModificationTrackers.FileTracker
    }
}
