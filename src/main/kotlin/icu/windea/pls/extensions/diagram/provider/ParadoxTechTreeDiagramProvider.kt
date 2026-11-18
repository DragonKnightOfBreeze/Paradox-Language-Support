package icu.windea.pls.extensions.diagram.provider

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramCategory
import com.intellij.diagram.DiagramPresentationModel
import com.intellij.diagram.DiagramRelationshipInfoAdapter
import com.intellij.diagram.presentation.DiagramLineType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.runBlockingCancellable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.util.progress.reportProgressScope
import com.intellij.platform.util.progress.reportSequentialProgress
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.ui.SimpleColoredText
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.base.ChronicleModificationTrackers
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.ep.util.data.StellarisTechnologyData
import icu.windea.pls.extensions.diagram.ChronicleDiagramBundle
import icu.windea.pls.extensions.diagram.OrderedDiagramNodeContentManager
import icu.windea.pls.extensions.diagram.ParadoxDefinitionDiagramNodeModel
import icu.windea.pls.extensions.diagram.ParadoxDiagramDataModel
import icu.windea.pls.extensions.diagram.ParadoxDiagramPropertyItem
import icu.windea.pls.extensions.diagram.getDiagramNodeModel
import icu.windea.pls.extensions.diagram.settings.ParadoxTechTreeDiagramSettings
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.presentation.ParadoxDefinitionPresentationUtil
import icu.windea.pls.lang.presentation.StellarisTechnologyCardAssets
import icu.windea.pls.lang.presentation.StellarisTechnologyCardBuilder
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.util.ParadoxTechnologyManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon
import javax.swing.JComponent

/**
 * 提供群星的科技树图表。
 * - 可以配置是否显示类型、关键属性、展示名字（科技名）、图形展示（科技卡）。
 * - 可以按类型、级别、分类、领域过滤要显示的科技。
 * - 可以按作用域过滤要显示的科技。（例如，仅限原版，仅限当前模组）
 * - 支持任何通用的图表操作。（例如，导出为图片）
 */
abstract class ParadoxTechTreeDiagramProvider(gameType: ParadoxGameType) : ParadoxDefinitionDiagramProvider(gameType) {
    private val _elementManager by lazy { ElementManager(this) }

    override fun createNodeContentManager() = NodeContentManager()

    override fun getElementManager() = _elementManager

    abstract override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel): DataModel

    override fun getAllContentCategories() = Categories.All

    abstract override fun getDiagramSettings(project: Project): ParadoxTechTreeDiagramSettings<*>?

    class NodeContentManager : OrderedDiagramNodeContentManager() {
        override fun isInCategory(nodeElement: Any?, item: Any?, category: DiagramCategory, builder: DiagramBuilder?): Boolean {
            return when (item) {
                is Items.Type -> category == Categories.Type
                is Items.Property -> category == Categories.Properties
                is Items.PresentableName -> category == Categories.PresentableName
                is Items.Presentation -> category == Categories.Presentation
                else -> true
            }
        }

        override fun getContentCategories(): Array<DiagramCategory> {
            return Categories.All
        }
    }

    class ElementManager(provider: ParadoxDefinitionDiagramProvider) : ParadoxDefinitionDiagramProvider.ElementManager(provider) {
        override fun isAcceptableAsNode(o: Any?): Boolean {
            return o is PsiDirectory || o is ParadoxScriptFile || o is ParadoxScriptProperty
        }

        override fun getElementTitle(element: PsiElement, builder: DiagramBuilder): String? {
            ProgressManager.checkCanceled()
            if (element is ParadoxScriptProperty) {
                // 直接读取预加载的节点展示模型，无需读锁
                return builder.getDiagramNodeModel(element)?.title
            }
            return super.getElementTitle(element, builder)
        }

        override fun getNodeItems(nodeElement: PsiElement?, builder: DiagramBuilder): Array<Any> {
            ProgressManager.checkCanceled()
            if (nodeElement == null) return emptyArray()
            val model = builder.getDiagramNodeModel(nodeElement) ?: return emptyArray()
            val result = mutableListOf<Any>()
            model.typeText?.let { result += Items.Type(it) }
            model.properties.forEach { result += Items.Property(it.text) }
            model.presentableName?.let { result += Items.PresentableName(it) }
            result += Items.Presentation(model)
            return result.toTypedArray()
        }

        override fun getItemComponent(nodeElement: PsiElement, nodeItem: Any?, builder: DiagramBuilder): JComponent? {
            ProgressManager.checkCanceled()
            return when (nodeItem) {
                is Items.PresentableName -> {
                    ParadoxDefinitionPresentationUtil.getLabel(nodeItem.text.orAnonymous())
                }
                is Items.Presentation -> {
                    buildPresentation(nodeElement, nodeItem.model, builder)
                }
                else -> null
            }
        }

        override fun getItemName(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder): SimpleColoredText? {
            ProgressManager.checkCanceled()
            return when (nodeItem) {
                is Items.Type -> {
                    SimpleColoredText(nodeItem.text, DEFAULT_TEXT_ATTR)
                }
                is Items.Property -> nodeItem.text
                else -> null
            }
        }

        override fun getItemIcon(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder?): Icon? {
            ProgressManager.checkCanceled()
            return when (nodeItem) {
                is Items.Type -> ChronicleIcons.Nodes.Type
                is Items.Property -> ChronicleIcons.Nodes.Property
                else -> null
            }
        }

        /**
         * 构建科技卡组件。
         *
         * - 若渲染资产已经就绪（预加载或先前物化），则零锁、零 IO 地直接组装。
         * - 若当前处于非 EDT 且非读锁的线程中，可以安全地同步物化并缓存。
         * - 若当前处于 EDT 或读锁中，则**绝不**同步阻塞，而是启动后台协程物化，并安全降级为 `null`，
         *   物化完成后再请求刷新图表。
         */
        private fun buildPresentation(
            nodeElement: PsiElement,
            model: ParadoxDefinitionDiagramNodeModel,
            builder: DiagramBuilder,
        ): JComponent? {
            val cardModel = model.cardModel ?: return null
            val element = nodeElement as? ParadoxDefinitionElement ?: return null
            val cardBuilder = StellarisTechnologyCardBuilder(element, builder.project)

            model.cardAssets?.let { assets -> return cardBuilder.build(cardModel, assets) }

            val application = ApplicationManager.getApplication()
            if (application.isDispatchThread || application.isReadAccessAllowed) {
                scheduleCardAssetsMaterialization(model, cardBuilder, builder)
                return null
            }

            // 非 EDT 且非读锁：可以安全地同步物化
            val assets = cardBuilder.materializeAssetsBlocking(cardModel) ?: return null
            model.cardAssets = assets
            return cardBuilder.build(cardModel, assets)
        }

        private fun scheduleCardAssetsMaterialization(
            model: ParadoxDefinitionDiagramNodeModel,
            cardBuilder: StellarisTechnologyCardBuilder,
            builder: DiagramBuilder,
        ) {
            if (model.cardAssetsFailed) return
            if (!model.cardAssetsLoading.compareAndSet(false, true)) return
            val cardModel = model.cardModel
            if (cardModel == null) {
                model.cardAssetsLoading.set(false)
                model.cardAssetsFailed = true
                return
            }
            val project = builder.project
            ChronicleFacade.getCoroutineScope(project).launch(Dispatchers.IO) {
                try {
                    val assets = cardBuilder.materializeAssets(cardModel)
                    if (assets == null) {
                        model.cardAssetsFailed = true
                    } else {
                        model.cardAssets = assets
                        // 资产就绪后请求一次展示刷新（若尚未调度）
                        (builder.dataModel as? ParadoxDiagramDataModel)?.schedulePresentationRefresh(builder)
                    }
                } finally {
                    model.cardAssetsLoading.set(false)
                }
            }
        }
    }

    abstract class DataModel(
        project: Project,
        file: VirtualFile?, // umlFile
        provider: ParadoxDefinitionDiagramProvider,
    ) : ParadoxDefinitionDiagramProvider.DataModel(project, file, provider) {
        private val definitionType = ParadoxDefinitionTypes.technology
        private val nodeMap = mutableMapOf<ParadoxDefinitionElement, Node>()
        private val techMap = mutableMapOf<String, ParadoxDefinitionElement>()

        override fun updateDataModel() {
            // 群星原版科技有400+

            val showPresentation = nodeContentManager.isCategoryEnabled(Categories.Presentation)

            val title = ChronicleDiagramBundle.message("techTree.update.title")
            runWithModalProgressBlocking(project, title) action@{
                reportSequentialProgress { sReporter ->
                    val step1 = ChronicleDiagramBundle.message("techTree.update.step.1")
                    val technologies = sReporter.indeterminateStep(step1) {
                        readAction { searchTechnologies() }
                    }
                    if (technologies.isEmpty()) return@action
                    val size = technologies.size

                    val step2 = ChronicleDiagramBundle.message("techTree.update.step.2", size)
                    sReporter.nextStep(25, step2) {
                        reportProgressScope(size) { reporter ->
                            technologies.forEach { technology ->
                                reporter.itemStep(step2) {
                                    readAction { createNode(technology) }
                                }
                            }
                        }
                    }

                    val step3 = ChronicleDiagramBundle.message("techTree.update.step.3", size)
                    sReporter.nextStep(50, step3) {
                        reportProgressScope(size) { reporter ->
                            technologies.forEach { technology ->
                                reporter.itemStep {
                                    readAction { createEdges(technology) }
                                }
                            }
                        }
                    }

                    val step4 = ChronicleDiagramBundle.message("techTree.update.step.4", size)
                    sReporter.nextStep(if (showPresentation) 75 else 100, step4) {
                        reportProgressScope(size) { reporter ->
                            technologies.forEach { technology ->
                                reporter.itemStep {
                                    readAction { preloadData(technology) }
                                }
                            }
                        }
                    }

                    // 关键三段隔离（第三步）：读锁释放后，在无锁的后台线程中物化科技卡渲染资产。
                    // 仅当图形展示类别处于启用状态时才预物化；否则在元素管理器中被按需安全地懒物化。
                    if (showPresentation) {
                        val step5 = ChronicleDiagramBundle.message("techTree.update.step.5", size)
                        sReporter.nextStep(100, step5) {
                            reportProgressScope(size) { reporter ->
                                technologies.forEach { technology ->
                                    reporter.itemStep {
                                        ProgressManager.checkCanceled()
                                        val model = getNodeModel(technology) ?: return@itemStep
                                        val cardModel = model.cardModel ?: return@itemStep
                                        if (model.cardAssets != null) return@itemStep
                                        val cardBuilder = StellarisTechnologyCardBuilder(technology, project)
                                        val assets: StellarisTechnologyCardAssets? = runBlockingCancellable {
                                            cardBuilder.materializeAssets(cardModel)
                                        }
                                        if (assets == null) {
                                            model.cardAssetsFailed = true
                                        } else {
                                            model.cardAssets = assets
                                        }
                                    }
                                }
                            }
                        }
                    }

                    nodeMap.clear()
                    techMap.clear()
                }
            }
        }

        private fun searchTechnologies(): List<ParadoxDefinitionElement> {
            ProgressManager.checkCanceled()
            val definitions = getDefinitions(definitionType)
            val settings = provider.getDiagramSettings(project)?.state
            return definitions.filter { settings == null || showNode(it, settings) }
        }

        private fun createNode(technology: ParadoxDefinitionElement) {
            ProgressManager.checkCanceled()
            val node = Node(technology, provider)
            nodeMap.put(technology, node)
            val name = technology.definitionInfo?.name.orAnonymous()
            techMap.put(name, technology)
            nodes.add(node)
        }

        private fun createEdges(technology: ParadoxDefinitionElement) {
            ProgressManager.checkCanceled()
            val data = technology.getDefinitionData<StellarisTechnologyData>() ?: return
            // 循环科技 ..> 循环科技
            val levels = data.levels
            if (levels != null) {
                val label = if (levels <= 0) "max level: inf" else "max level: $levels"
                val node = nodeMap.get(technology) ?: return
                val edge = Edge(node, node, Relations.Repeat(label))
                edges.add(edge)
            }
            // 前置 --> 科技
            val prerequisites = data.prerequisites
            prerequisites.forEach { prerequisite ->
                ProgressManager.checkCanceled()
                val source = techMap.get(prerequisite)?.let { nodeMap.get(it) } ?: return@forEach
                val target = nodeMap.get(technology) ?: return@forEach
                val edge = Edge(source, target, Relations.Prerequisite)
                edges.add(edge)
            }
        }

        /**
         * 在读锁阶段一次性预加载节点的全部展示数据（含科技数据与科技卡呈现模型），
         * 避免渲染阶段再次访问 PSI 并加读锁。
         */
        private fun preloadData(technology: ParadoxDefinitionElement) {
            ProgressManager.checkCanceled()
            val definitionInfo = technology.definitionInfo
            val techData = technology.getDefinitionData<StellarisTechnologyData>()
            val keys = provider.getItemPropertyKeys()
            val detailKeys = provider.getItemPropertyKeysInDetail()
            val properties = ParadoxDefinitionPresentationUtil.getProperties(technology, keys).map { property ->
                ParadoxDiagramPropertyItem(ParadoxDefinitionPresentationUtil.getPropertyText(property, property.name in detailKeys))
            }
            val cardModel = StellarisTechnologyCardBuilder(technology, project).loadModel()
            val model = ParadoxDefinitionDiagramNodeModel(
                title = ParadoxTechnologyManager.getName(technology),
                typeText = definitionInfo?.typeText,
                presentableName = ParadoxDefinitionPresentationUtil.getNameText(technology),
                subtypes = definitionInfo?.subtypes?.toSet().orEmpty(),
                properties = properties,
                techArea = techData?.area,
                cardModel = cardModel,
            )
            putNodeModel(technology, model)
        }

        override fun getModificationTracker(): ModificationTracker {
            val configGroup = ChronicleFacade.getConfigGroup(project, provider.gameType)
            return ChronicleModificationTrackers.scriptFileFromDefinitionTypes(configGroup, definitionType)
        }
    }

    object Categories {
        val Type = DiagramCategory(ChronicleDiagramBundle.lazyMessage("techTree.category.type"), ChronicleIcons.Nodes.Type, true, false)
        val Properties = DiagramCategory(ChronicleDiagramBundle.lazyMessage("techTree.category.properties"), ChronicleIcons.Nodes.Property, true, false)
        val PresentableName = DiagramCategory(ChronicleDiagramBundle.lazyMessage("techTree.category.presentableName"), ChronicleIcons.Nodes.Localisation, false, false)
        val Presentation = DiagramCategory(ChronicleDiagramBundle.lazyMessage("techTree.category.presentation"), ChronicleIcons.General.Presentation, false, false)

        val All = arrayOf(Type, Properties, PresentableName, Presentation)
    }

    object Relations {
        val Prerequisite = object : DiagramRelationshipInfoAdapter("PREREQUISITE", DiagramLineType.SOLID) {
            override fun getTargetArrow() = DELTA
        }

        private val repeatCache = ConcurrentHashMap<String, DiagramRelationshipInfoAdapter>()
        fun Repeat(label: String) = repeatCache.getOrPut(label) {
            object : DiagramRelationshipInfoAdapter("REPEAT", DiagramLineType.DOTTED, label) {
                override fun getTargetArrow() = DELTA
            }
        }
    }

    object Items {
        class Type(val text: String)
        class Property(val text: SimpleColoredText)
        class PresentableName(val text: String)
        class Presentation(val model: ParadoxDefinitionDiagramNodeModel)
    }
}
