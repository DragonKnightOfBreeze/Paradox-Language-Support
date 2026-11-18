package icu.windea.pls.extensions.diagram

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramDataKeys
import com.intellij.diagram.DiagramDataModel
import com.intellij.diagram.DiagramNode
import com.intellij.openapi.application.runInEdt
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileSystemItem
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.extensions.diagram.provider.ParadoxDiagramProvider
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

// com.intellij.uml.java.JavaUmlDataModel

/**
 * 图表数据模型的基类。
 *
 * 注意：[refreshDataModel] **不**会在外部包裹读锁。各个具体的数据模型应在
 * `updateDataModel()` 中精确地分段获取 [com.intellij.openapi.application.readAction]，
 * 并在读锁之外执行无锁的后台任务（例如在 `Dispatchers.IO` 中物化图片），
 * 从而避免在读锁内调用 `runWithModalProgressBlocking` 导致平台死锁。
 *
 * 预加载后的节点展示模型由 [nodeModels] 持有，供元素管理器在无锁状态下读取。
 */
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

    // 预加载的节点展示模型。以 PSI 元素为键，O(1) 读取，供元素管理器在无锁状态下使用。
    private val nodeModels = ConcurrentHashMap<PsiElement, ParadoxDefinitionDiagramNodeModel>()

    // 用于合并异步物化科技卡资产后的展示刷新请求，避免每个节点各自触发一次完整刷新。
    private val presentationRefreshScheduled = AtomicBoolean(false)

    @Volatile
    private var disposed = false

    override fun getNodes() = _nodes

    override fun getEdges() = _edges

    override fun getNodeName(node: DiagramNode<PsiElement>) = node.tooltip.orAnonymous()

    override fun addElement(element: PsiElement?) = null

    override fun dispose() {
        disposed = true
        cleanAllNodeAndEdges()
    }

    override fun refreshDataModel() {
        ProgressManager.checkCanceled()
        synchronized(lock) {
            cleanAllNodeAndEdges()
            // NOTE 3.0.6 移除外部的 `runSmartReadAction` 包裹，避免在读锁内调用
            // `runWithModalProgressBlocking` 导致平台死锁。读锁由各具体数据模型精确分段控制。
            updateDataModel()
        }
    }

    /**
     * 得到指定元素对应的预加载节点展示模型，可以在无锁状态下调用。
     */
    fun getNodeModel(element: PsiElement): ParadoxDefinitionDiagramNodeModel? {
        return nodeModels[element]
    }

    /**
     * 注册指定元素的预加载节点展示模型。通常在 `updateDataModel()` 的读锁阶段调用。
     */
    protected fun putNodeModel(element: PsiElement, model: ParadoxDefinitionDiagramNodeModel) {
        nodeModels[element] = model
    }

    /**
     * 请求一次展示刷新，用于异步物化资产后更新节点。多次请求会被合并。
     *
     * 该方法只会从 EDT 调度一次刷新，绝不在这里执行读锁或阻塞操作。
     */
    fun schedulePresentationRefresh(builder: DiagramBuilder) {
        if (!presentationRefreshScheduled.compareAndSet(false, true)) return
        val project = builder.project
        runInEdt action@{
            presentationRefreshScheduled.set(false)
            if (disposed || project.isDisposed) return@action
            runCatching {
                builder.queryUpdate().withPresentationUpdate().withAllNodeSizeUpdate().run()
            }
        }
    }

    protected open fun cleanAllNodeAndEdges() {
        nodes.clear()
        edges.clear()
        nodeModels.clear()
    }

    protected abstract fun updateDataModel()

    override fun getModificationTracker(): ModificationTracker {
        return ChronicleModificationTrackers.ScriptFile
    }
}
