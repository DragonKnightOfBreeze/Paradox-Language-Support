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
import icu.windea.pls.ep.data.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.StellarisTechTreeDiagramProvider.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*
import javax.swing.*

/**
 * 提供群星的科技树图表。
 * * 可以配置是否显示UI表示（科技卡图片）、科技名、图标、关键属性。
 * * 可以按类型、级别、分类、领域过滤要显示的科技。
 * * 可以按作用域过滤要显示的科技。（例如，仅限原版，仅限当前模组）
 * * 支持任何通用的图表操作。（例如，导出为图片）
 */
abstract class ParadoxTechTreeDiagramProvider(gameType: ParadoxGameType) : ParadoxDefinitionDiagramProvider(gameType) {
    object Categories {
        val Type = DiagramCategory(PlsDiagramBundle.lazyMessage("techTree.category.type"), PlsIcons.Nodes.Type, true, false)
        val Properties = DiagramCategory(PlsDiagramBundle.lazyMessage("techTree.category.properties"), PlsIcons.Nodes.Property, true, false)
        val Name = DiagramCategory(PlsDiagramBundle.lazyMessage("techTree.category.name"), PlsIcons.Nodes.Localisation, false, false)
        val Presentation = DiagramCategory(PlsDiagramBundle.lazyMessage("techTree.category.presentation"), PlsIcons.General.Presentation, false, false)

        val All = arrayOf(Type, Properties, Name, Presentation)
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
        class Type(val definition: ParadoxScriptProperty)

        class Property(val property: ParadoxScriptProperty)

        class Name(val definition: ParadoxScriptProperty)

        class Presentation(val definition: ParadoxScriptProperty)
    }

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
                is Items.Name -> category == Categories.Name
                is Items.Presentation -> category == Categories.Presentation
                else -> true
            }
        }

        override fun getContentCategories(): Array<DiagramCategory> {
            return Categories.All
        }
    }

    class ElementManager(provider: ParadoxDiagramProvider) : ParadoxDiagramElementManager(provider) {
        override fun isAcceptableAsNode(o: Any?): Boolean {
            return o is PsiDirectory || o is ParadoxScriptFile || o is ParadoxScriptProperty
        }

        override fun getElementTitle(element: PsiElement): String? {
            ProgressManager.checkCanceled()
            return when (element) {
                is ParadoxScriptProperty -> runReadAction { ParadoxTechnologyManager.getName(element) }
                else -> super.getElementTitle(element)
            }
        }

        override fun getNodeItems(nodeElement: PsiElement?, builder: DiagramBuilder): Array<Any> {
            provider as ParadoxTechTreeDiagramProvider
            ProgressManager.checkCanceled()
            return when (nodeElement) {
                is ParadoxScriptProperty -> {
                    val result = mutableListOf<Any>()
                    result += Items.Type(nodeElement)
                    val properties = runReadAction { provider.getProperties(nodeElement) }
                    properties.forEach { result += Items.Property(it) }
                    result += Items.Name(nodeElement)
                    result += Items.Presentation(nodeElement)
                    result.toTypedArray()
                }
                else -> emptyArray()
            }
        }

        override fun getItemComponent(nodeElement: PsiElement, nodeItem: Any?, builder: DiagramBuilder): JComponent? {
            ProgressManager.checkCanceled()
            return when (nodeItem) {
                is Items.Name -> runReadAction r@{
                    val nameText = ParadoxPresentationManager.getNameText(nodeItem.definition) ?: return@r null
                    val result = ParadoxPresentationManager.getLabel(nameText.or.anonymous())
                    result
                }
                is Items.Presentation -> runReadAction r@{
                    val definition = nodeItem.definition
                    val result = ParadoxPresentationManager.getPresentation(definition)
                    result
                }
                else -> null
            }
        }

        override fun getItemName(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder): SimpleColoredText? {
            ProgressManager.checkCanceled()
            return when (nodeItem) {
                is Items.Type -> runReadAction r@{
                    val typesText = nodeItem.definition.definitionInfo?.typesText ?: return@r null
                    val result = SimpleColoredText(typesText, DEFAULT_TEXT_ATTR)
                    result
                }
                is Items.Property -> runReadAction {
                    val property = nodeItem.property
                    val rendered = ParadoxScriptTextRenderer().render(property)
                    val result = SimpleColoredText(rendered, DEFAULT_TEXT_ATTR)
                    val propertyValue = property.propertyValue
                    if (propertyValue is ParadoxScriptScriptedVariableReference) {
                        val sv = propertyValue.text
                        result.append(" by $sv", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                    }
                    result
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

        @Suppress("RedundantOverride")
        override fun getItemDocOwner(element: Any?, builder: DiagramBuilder): PsiElement? {
            //property -> No documentation found -> ok
            return super.getItemDocOwner(element, builder)
        }

        override fun getNodeTooltip(element: PsiElement?): String? {
            return null
        }
    }

    abstract class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxDefinitionDiagramProvider.DataModel(project, file, provider) {
        private val definitionType = ParadoxDefinitionTypes.Technology
        private val nodeMap = mutableMapOf<ParadoxScriptDefinitionElement, Node>()
        private val techMap = mutableMapOf<String, ParadoxScriptDefinitionElement>()

        override fun updateDataModel() {
            //群星原版科技有400+

            val title = PlsDiagramBundle.message("techTree.update.title")
            runWithModalProgressBlocking(project, title) action@{
                reportSequentialProgress { sReporter ->
                    val step1 = PlsDiagramBundle.message("techTree.update.step.1")
                    val technologies = sReporter.indeterminateStep(step1) {
                        readAction { searchTechnologies() }
                    }
                    if (technologies.isEmpty()) return@action
                    val size = technologies.size

                    val step2 = PlsDiagramBundle.message("techTree.update.step.2", size)
                    sReporter.nextStep(25, step2) {
                        reportProgress(size) { reporter ->
                            technologies.forEach { technology ->
                                reporter.itemStep(step2) {
                                    readAction { createNode(technology) }
                                }
                            }
                        }
                    }

                    val step3 = PlsDiagramBundle.message("techTree.update.step.3", size)
                    sReporter.nextStep(50, step3) {
                        reportProgress(size) { reporter ->
                            technologies.forEach { technology ->
                                reporter.itemStep {
                                    readAction { createEdges(technology) }
                                }
                            }
                        }
                    }

                    val step4 = PlsDiagramBundle.message("techTree.update.step.4", size)
                    sReporter.nextStep(100, step4) {
                        reportProgress(size) { reporter ->
                            technologies.forEach { technology ->
                                reporter.itemStep {
                                    readAction { preloadLocalisations(technology) }
                                }
                            }
                        }
                    }

                    nodeMap.clear()
                    techMap.clear()
                }
            }
        }

        private fun searchTechnologies(): List<ParadoxScriptDefinitionElement> {
            ProgressManager.checkCanceled()
            val definitions = getDefinitions(definitionType)
            val settings = provider.getDiagramSettings(project)?.state
            return definitions.filter { settings == null || showNode(it, settings) }
        }

        private fun createNode(technology: ParadoxScriptDefinitionElement) {
            ProgressManager.checkCanceled()
            provider as ParadoxDefinitionDiagramProvider
            val node = Node(technology, provider)
            val data = technology.getData<StellarisTechnologyData>()
            node.putUserData(Keys.nodeData, data)
            nodeMap.put(technology, node)
            val name = technology.definitionInfo?.name.or.anonymous()
            techMap.put(name, technology)
            nodes.add(node)
        }

        private fun createEdges(technology: ParadoxScriptDefinitionElement) {
            ProgressManager.checkCanceled()
            val data = technology.getData<StellarisTechnologyData>() ?: return
            //循环科技 ..> 循环科技
            val levels = data.levels
            if (levels != null) {
                val label = if (levels <= 0) "max level: inf" else "max level: $levels"
                val node = nodeMap.get(technology) ?: return
                val edge = Edge(node, node, Relations.Repeat(label))
                edges.add(edge)
            }
            //前置 --> 科技
            val prerequisites = data.prerequisites
            prerequisites.forEach { prerequisite ->
                ProgressManager.checkCanceled()
                val source = techMap.get(prerequisite)?.let { nodeMap.get(it) } ?: return@forEach
                val target = nodeMap.get(technology) ?: return@forEach
                val edge = Edge(source, target, Relations.Prerequisite)
                edges.add(edge)
            }
        }

        private fun preloadLocalisations(technology: ParadoxScriptDefinitionElement) {
            ProgressManager.checkCanceled()
            ParadoxPresentationManager.getNameLocalisation(technology)
        }

        override fun getModificationTracker(): ModificationTracker {
            val configGroup = PlsFacade.getConfigGroup(project, provider.gameType)
            val typeConfig = configGroup.types.get(definitionType) ?: return super.getModificationTracker()
            val key = CwtConfigManager.getFilePathPatterns(typeConfig).joinToString(";")
            return ParadoxModificationTrackers.ScriptFileTracker(key)
        }
    }
}
