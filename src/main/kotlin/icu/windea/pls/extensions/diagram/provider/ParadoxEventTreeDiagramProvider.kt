package icu.windea.pls.extensions.diagram.provider

import com.intellij.diagram.DiagramBuilder
import com.intellij.diagram.DiagramCategory
import com.intellij.diagram.DiagramPresentationModel
import com.intellij.diagram.DiagramRelationshipInfoAdapter
import com.intellij.diagram.presentation.DiagramLineType
import com.intellij.openapi.application.readAction
import com.intellij.openapi.progress.ProgressManager
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
import icu.windea.pls.extensions.diagram.ChronicleDiagramBundle
import icu.windea.pls.extensions.diagram.OrderedDiagramNodeContentManager
import icu.windea.pls.extensions.diagram.ParadoxDefinitionDiagramNodeModel
import icu.windea.pls.extensions.diagram.ParadoxDiagramPropertyItem
import icu.windea.pls.extensions.diagram.getDiagramNodeModel
import icu.windea.pls.extensions.diagram.settings.ParadoxEventTreeDiagramSettings
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.presentation.ParadoxDefinitionPresentationUtil
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import javax.swing.Icon
import javax.swing.JComponent

/**
 * 提供事件树图表。
 * - 可以配置是否显示类型、关键属性、展示名字（事件标题）。
 * - 可以按类型过滤要显示的事件。
 * - 可以按作用域过滤要显示的事件。（例如，仅限原版，仅限当前模组）
 * - 支持任何通用的图表操作。（例如，导出为图片）
 */
abstract class ParadoxEventTreeDiagramProvider(gameType: ParadoxGameType) : ParadoxDefinitionDiagramProvider(gameType) {
    private val _elementManager by lazy { ElementManager(this) }

    override fun createNodeContentManager() = NodeContentManager()

    override fun getElementManager() = _elementManager

    abstract override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel): DataModel

    override fun getAllContentCategories() = Categories.All

    abstract override fun getDiagramSettings(project: Project): ParadoxEventTreeDiagramSettings<*>?

    class NodeContentManager : OrderedDiagramNodeContentManager() {
        override fun isInCategory(nodeElement: Any?, item: Any?, category: DiagramCategory, builder: DiagramBuilder?): Boolean {
            return when (item) {
                is Items.Type -> category == Categories.Type
                is Items.Property -> category == Categories.Properties
                is Items.PresentableName -> category == Categories.PresentableName
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
            return result.toTypedArray()
        }

        override fun getItemComponent(nodeElement: PsiElement, nodeItem: Any?, builder: DiagramBuilder): JComponent? {
            ProgressManager.checkCanceled()
            return when (nodeItem) {
                is Items.PresentableName -> {
                    ParadoxDefinitionPresentationUtil.getLabel(nodeItem.text.orAnonymous())
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
    }

    abstract class DataModel(
        project: Project,
        file: VirtualFile?, // umlFile
        provider: ParadoxDefinitionDiagramProvider,
    ) : ParadoxDefinitionDiagramProvider.DataModel(project, file, provider) {
        val definitionType = ParadoxDefinitionTypes.event
        private val nodeMap = mutableMapOf<ParadoxDefinitionElement, Node>()
        private val eventMap = mutableMapOf<String, ParadoxDefinitionElement>()

        override fun updateDataModel() {
            // 群星原版事件有5000+

            val title = ChronicleDiagramBundle.message("eventTree.update.title")
            runWithModalProgressBlocking(project, title) action@{
                reportSequentialProgress { sReporter ->
                    val step1 = ChronicleDiagramBundle.message("eventTree.update.step.1")
                    val events = sReporter.indeterminateStep(step1) {
                        readAction { searchEvents() }
                    }
                    if (events.isEmpty()) return@action
                    val size = events.size

                    val step2 = ChronicleDiagramBundle.message("eventTree.update.step.2", size)
                    sReporter.nextStep(25, step2) {
                        reportProgressScope(size) { reporter ->
                            events.forEach { event ->
                                reporter.itemStep(step2) {
                                    readAction { createNode(event) }
                                }
                            }
                        }
                    }

                    val step3 = ChronicleDiagramBundle.message("eventTree.update.step.3", size)
                    sReporter.nextStep(50, step3) {
                        reportProgressScope(size) { reporter ->
                            events.forEach { event ->
                                reporter.itemStep {
                                    readAction { createEdges(event) }
                                }
                            }
                        }
                    }

                    val step4 = ChronicleDiagramBundle.message("eventTree.update.step.4", size)
                    sReporter.nextStep(100, step4) {
                        reportProgressScope(size) { reporter ->
                            events.forEach { event ->
                                reporter.itemStep {
                                    readAction { preloadData(event) }
                                }
                            }
                        }
                    }

                    nodeMap.clear()
                    eventMap.clear()
                }
            }
        }

        private fun searchEvents(): List<ParadoxDefinitionElement> {
            ProgressManager.checkCanceled()
            val definitions = getDefinitions(definitionType)
            val settings = provider.getDiagramSettings(project)?.state
            return definitions.filter { settings == null || showNode(it, settings) }
        }

        private fun createNode(event: ParadoxDefinitionElement): Boolean {
            ProgressManager.checkCanceled()
            val node = Node(event, provider)
            nodeMap.put(event, node)
            val name = event.definitionInfo?.name.orAnonymous()
            eventMap.put(name, event)
            return nodes.add(node)
        }

        private fun createEdges(event: ParadoxDefinitionElement) {
            ProgressManager.checkCanceled()
            // 事件 --> 调用的事件
            val invocations = ParadoxEventManager.getInvocations(event)
            invocations.forEach { invocation ->
                ProgressManager.checkCanceled()
                val source = nodeMap.get(event) ?: return@forEach
                val target = eventMap.get(invocation)?.let { nodeMap.get(it) } ?: return@forEach
                val edge = Edge(source, target, Relations.Invoke)
                edges.add(edge)
            }
        }

        /**
         * 在读锁阶段一次性预加载节点的全部展示数据，避免渲染阶段再次访问 PSI 并加读锁。
         */
        private fun preloadData(event: ParadoxDefinitionElement) {
            ProgressManager.checkCanceled()
            val definitionInfo = event.definitionInfo
            val keys = provider.getItemPropertyKeys()
            val detailKeys = provider.getItemPropertyKeysInDetail()
            val properties = ParadoxDefinitionPresentationUtil.getProperties(event, keys).map { property ->
                ParadoxDiagramPropertyItem(ParadoxDefinitionPresentationUtil.getPropertyText(property, property.name in detailKeys))
            }
            val model = ParadoxDefinitionDiagramNodeModel(
                title = ParadoxEventManager.getName(event),
                typeText = definitionInfo?.typeText,
                presentableName = ParadoxDefinitionPresentationUtil.getNameText(event),
                subtypes = definitionInfo?.subtypes?.toSet().orEmpty(),
                properties = properties,
            )
            putNodeModel(event, model)
        }

        override fun getModificationTracker(): ModificationTracker {
            val configGroup = ChronicleFacade.getConfigGroup(project, provider.gameType)
            return ChronicleModificationTrackers.scriptFileFromDefinitionTypes(configGroup, definitionType)
        }
    }

    object Categories {
        val Type = DiagramCategory(ChronicleDiagramBundle.lazyMessage("eventTree.category.type"), ChronicleIcons.Nodes.Type, true, false)
        val Properties = DiagramCategory(ChronicleDiagramBundle.lazyMessage("eventTree.category.properties"), ChronicleIcons.Nodes.Property, true, false)
        val PresentableName = DiagramCategory(ChronicleDiagramBundle.lazyMessage("eventTree.category.presentableName"), ChronicleIcons.Nodes.Localisation, false, false)

        val All = arrayOf(Type, Properties, PresentableName)
    }

    object Relations {
        val Invoke = object : DiagramRelationshipInfoAdapter("INVOKE", DiagramLineType.SOLID, ChronicleDiagramBundle.message("eventTree.rel.invoke")) {
            override fun getTargetArrow() = DELTA
        }
    }

    object Items {
        class Type(val text: String)
        class Property(val text: SimpleColoredText)
        class PresentableName(val text: String)
    }
}
