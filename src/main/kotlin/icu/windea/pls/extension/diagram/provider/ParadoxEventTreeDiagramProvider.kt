package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.presentation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.platform.util.progress.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 提供事件树图表。
 * * 可以配置是否显示类型、关键属性、本地化后的名字（事件标题）。
 * * 可以按类型过滤要显示的事件。
 * * 可以按作用域过滤要显示的事件。（例如，仅限原版，仅限当前模组）
 * * 支持任何通用的图表操作。（例如，导出为图片）
 */
abstract class ParadoxEventTreeDiagramProvider(gameType: ParadoxGameType) : ParadoxDefinitionDiagramProvider(gameType) {
    object Categories {
        val Type = DiagramCategory(PlsDiagramBundle.lazyMessage("eventTree.category.type"), PlsIcons.Nodes.Type, true, false)
        val Properties = DiagramCategory(PlsDiagramBundle.lazyMessage("eventTree.category.properties"), PlsIcons.Nodes.Property, true, false)
        val LocalizedName = DiagramCategory(PlsDiagramBundle.lazyMessage("eventTree.category.localizedName"), PlsIcons.Nodes.Localisation, false, false)

        val All = arrayOf(Type, Properties, LocalizedName)
    }

    object Relations {
        val Invoke = object : DiagramRelationshipInfoAdapter("INVOKE", DiagramLineType.SOLID, PlsDiagramBundle.message("eventTree.rel.invoke")) {
            override fun getTargetArrow() = DELTA
        }
    }

    object Items {
        class Type(val text: String)

        class Property(val property: ParadoxScriptProperty, val detail: Boolean)

        class LocalizedName(val text: String)
    }

    object Keys : KeyRegistry() {
        val typeText by createKey<String>(Keys)
        val nameText by createKey<String>(Keys)
    }

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
                is Items.LocalizedName -> category == Categories.LocalizedName
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

        override fun getElementTitle(element: PsiElement): String? {
            ProgressManager.checkCanceled()
            return when (element) {
                is ParadoxScriptProperty -> runReadAction { ParadoxEventManager.getName(element) }
                else -> super.getElementTitle(element)
            }
        }

        override fun getNodeItems(nodeElement: PsiElement?, builder: DiagramBuilder): Array<Any> {
            ProgressManager.checkCanceled()
            return when (nodeElement) {
                is ParadoxScriptProperty -> {
                    val result = mutableListOf<Any>()
                    nodeElement.getUserData(Keys.typeText)?.let { result += Items.Type(it) }
                    runReadAction {
                        val properties = ParadoxPresentationManager.getProperties(nodeElement, provider.getItemPropertyKeys())
                        properties.forEach { result += Items.Property(it, it.name in provider.getItemPropertyKeysInDetail()) }
                    }
                    nodeElement.getUserData(Keys.nameText)?.let { result += Items.LocalizedName(it) }
                    result.toTypedArray()
                }
                else -> emptyArray()
            }
        }

        override fun getItemComponent(nodeElement: PsiElement, nodeItem: Any?, builder: DiagramBuilder): JComponent? {
            ProgressManager.checkCanceled()
            return when (nodeItem) {
                is Items.LocalizedName -> {
                    ParadoxPresentationManager.getLabel(nodeItem.text.or.anonymous())
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
                is Items.Property -> runReadAction {
                    val propertyText = ParadoxPresentationManager.getPropertyText(nodeItem.property, nodeItem.detail)
                    propertyText
                }
                else -> null
            }
        }

        override fun getItemIcon(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder?): Icon? {
            ProgressManager.checkCanceled()
            return when (nodeItem) {
                is Items.Type -> PlsIcons.Nodes.Type
                is Items.Property -> PlsIcons.Nodes.Property
                else -> null
            }
        }
    }

    abstract class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxDefinitionDiagramProvider.DataModel(project, file, provider) {
        val definitionType = ParadoxDefinitionTypes.Event
        private val nodeMap = mutableMapOf<ParadoxScriptDefinitionElement, Node>()
        private val eventMap = mutableMapOf<String, ParadoxScriptDefinitionElement>()

        override fun updateDataModel() {
            //群星原版事件有5000+

            val title = PlsDiagramBundle.message("eventTree.update.title")
            runWithModalProgressBlocking(project, title) action@{
                reportSequentialProgress { sReporter ->
                    val step1 = PlsDiagramBundle.message("eventTree.update.step.1")
                    val events = sReporter.indeterminateStep(step1) {
                        readAction { searchEvents() }
                    }
                    if (events.isEmpty()) return@action
                    val size = events.size

                    val step2 = PlsDiagramBundle.message("eventTree.update.step.2", size)
                    sReporter.nextStep(25, step2) {
                        reportProgress(size) { reporter ->
                            events.forEach { event ->
                                reporter.itemStep(step2) {
                                    readAction { createNode(event) }
                                }
                            }
                        }
                    }

                    val step3 = PlsDiagramBundle.message("eventTree.update.step.3", size)
                    sReporter.nextStep(50, step3) {
                        reportProgress(size) { reporter ->
                            events.forEach { event ->
                                reporter.itemStep {
                                    readAction { createEdges(event) }
                                }
                            }
                        }
                    }

                    val step4 = PlsDiagramBundle.message("eventTree.update.step.4", size)
                    sReporter.nextStep(100, step4) {
                        reportProgress(size) { reporter ->
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

        private fun searchEvents(): List<ParadoxScriptDefinitionElement> {
            ProgressManager.checkCanceled()
            val definitions = getDefinitions(definitionType)
            val settings = provider.getDiagramSettings(project)?.state
            return definitions.filter { settings == null || showNode(it, settings) }
        }

        private fun createNode(event: ParadoxScriptDefinitionElement): Boolean {
            ProgressManager.checkCanceled()
            val node = Node(event, provider)
            nodeMap.put(event, node)
            val name = event.definitionInfo?.name.or.anonymous()
            eventMap.put(name, event)
            return nodes.add(node)
        }

        private fun createEdges(event: ParadoxScriptDefinitionElement) {
            ProgressManager.checkCanceled()
            //事件 --> 调用的事件
            val invocations = ParadoxEventManager.getInvocations(event)
            invocations.forEach { invocation ->
                ProgressManager.checkCanceled()
                val source = nodeMap.get(event) ?: return@forEach
                val target = eventMap.get(invocation)?.let { nodeMap.get(it) } ?: return@forEach
                val edge = Edge(source, target, Relations.Invoke)
                edges.add(edge)
            }
        }

        private fun preloadData(event: ParadoxScriptDefinitionElement) {
            ProgressManager.checkCanceled()
            run {
                val result = event.definitionInfo?.typesText
                event.putUserData(Keys.typeText, result)
            }
            run {
                val result = ParadoxPresentationManager.getNameText(event)
                event.putUserData(Keys.nameText, result)
            }
        }

        override fun getModificationTracker(): ModificationTracker {
            val configGroup = PlsFacade.getConfigGroup(project, provider.gameType)
            val typeConfig = configGroup.types.get(definitionType) ?: return super.getModificationTracker()
            val key = CwtConfigManager.getFilePathPatterns(typeConfig).joinToString(";")
            return ParadoxModificationTrackers.ScriptFileTracker(key)
        }
    }
}
