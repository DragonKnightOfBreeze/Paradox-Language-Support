package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.concurrency.*
import icu.windea.pls.extension.diagram.provider.*
import icu.windea.pls.lang.*
import org.jetbrains.concurrency.*
import java.util.concurrent.*
import java.util.function.Function

//com.intellij.uml.java.JavaUmlDataModel

abstract class ParadoxDiagramDataModel(
    project: Project,
    val file: VirtualFile?,
    val provider: ParadoxDiagramProvider,
): DiagramDataModel<PsiElement>(project, provider)  {
    val gameType get() = provider.gameType
    
    //rootFile - PsiFileSystemItem
    val originalFile get() = file?.getUserData(DiagramDataKeys.ORIGINAL_ELEMENT) as? PsiFileSystemItem
    
    private val _nodes = mutableSetOf<ParadoxDiagramNode>()
    private val _edges = mutableSetOf<ParadoxDiagramEdge>()
    private val lock = Any()
    
    override fun getNodes() = _nodes
    
    override fun getEdges() = _edges
    
    override fun getNodeName(node: DiagramNode<PsiElement>) = node.tooltip.orAnonymous()
    
    override fun addElement(element: PsiElement?) = null
    
    override fun dispose() {
        
    }
    
    override fun refreshDataModelAsync(indicator: ProgressIndicator): CompletableFuture<Void> {
        return refreshDataModel(indicator)
    }
    
    override fun refreshDataModel() {
        refreshDataModel(null)
    }
    
    fun refreshDataModel(indicator: ProgressIndicator?): CompletableFuture<Void> {
        ProgressManager.checkCanceled()
        val refreshInLock = Callable<CompletableFuture<Void>> { 
            ApplicationManager.getApplication().assertReadAccessAllowed()
            synchronized(lock) {
                cleanAllNodeAndEdges()
                if(indicator != null) indicator.isIndeterminate = true
                ProgressManager.checkCanceled()
                updateDataModel(indicator)
                CompletableFuture.completedFuture(null)
            }
        }
        if(ApplicationManager.getApplication().isReadAccessAllowed) {
            return refreshInLock.call()
        } else {
            var action = ReadAction.nonBlocking(refreshInLock).expireWith(this).inSmartMode(project).withDocumentsCommitted(project)
            if(indicator != null) action = action.wrapProgress(indicator)
            return action.submit(AppExecutorUtil.getAppExecutorService()).asCompletableFuture().thenComposeAsync(Function.identity())
        }
    }
    
    protected abstract fun updateDataModel(indicator: ProgressIndicator?)
    
    fun cleanAllNodeAndEdges() {
        nodes.clear()
        edges.clear()
    }
}
