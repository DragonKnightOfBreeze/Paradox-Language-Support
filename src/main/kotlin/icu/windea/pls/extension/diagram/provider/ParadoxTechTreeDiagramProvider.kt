package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.presentation.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.data.*
import icu.windea.pls.ep.presentation.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.provider.StellarisTechTreeDiagramProvider.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
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
    companion object {
        val CAT_TYPE = DiagramCategory(PlsDiagramBundle.lazyMessage("techTree.category.type"), PlsIcons.Nodes.Type, true, false)
        val CAT_PROPERTIES = DiagramCategory(PlsDiagramBundle.lazyMessage("techTree.category.properties"), PlsIcons.Nodes.Property, true, false)
        val CAT_NAME = DiagramCategory(PlsDiagramBundle.lazyMessage("techTree.category.name"), PlsIcons.Nodes.Localisation, false, false)
        val CAT_ICON = DiagramCategory(PlsDiagramBundle.lazyMessage("techTree.category.icon"), PlsIcons.General.Image, false, false)
        val CAT_PRESENTATION = DiagramCategory(PlsDiagramBundle.lazyMessage("techTree.category.presentation"), PlsIcons.General.Presentation, false, false)
        val CATEGORIES = arrayOf(CAT_TYPE, CAT_PROPERTIES, CAT_NAME, CAT_ICON, CAT_PRESENTATION)

        val REL_PREREQUISITE = object : DiagramRelationshipInfoAdapter("PREREQUISITE", DiagramLineType.SOLID) {
            override fun getTargetArrow() = DELTA
        }
        val REL_REPEAT_CACHE = ConcurrentHashMap<String, DiagramRelationshipInfoAdapter>()
        fun REL_REPEAT(label: String) = REL_REPEAT_CACHE.getOrPut(label) {
            object : DiagramRelationshipInfoAdapter("REPEAT", DiagramLineType.DOTTED, label) {
                override fun getTargetArrow() = DELTA
            }
        }
    }

    private val _elementManager by lazy { ElementManager(this) }

    override fun createNodeContentManager() = NodeContentManager()

    override fun getElementManager() = _elementManager

    abstract override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel): DataModel

    override fun getAllContentCategories() = CATEGORIES

    abstract override fun getDiagramSettings(project: Project): ParadoxTechTreeDiagramSettings<*>?

    class NodeContentManager : OrderedDiagramNodeContentManager() {
        override fun isInCategory(nodeElement: Any?, item: Any?, category: DiagramCategory, builder: DiagramBuilder?): Boolean {
            return when {
                item is CwtProperty -> category == CAT_TYPE
                item is ParadoxScriptProperty -> {
                    val definitionInfo = item.definitionInfo
                    if (definitionInfo != null) {
                        category == CAT_PRESENTATION
                    } else {
                        category == CAT_PROPERTIES
                    }
                }
                item is ParadoxLocalisationProperty -> category == CAT_NAME
                item is PsiFile -> category == CAT_ICON
                else -> false
            }
        }

        override fun getContentCategories(): Array<DiagramCategory> {
            return CATEGORIES
        }
    }

    class ElementManager(provider: ParadoxDiagramProvider) : ParadoxDiagramElementManager(provider) {
        override fun isAcceptableAsNode(o: Any?): Boolean {
            return o is PsiDirectory || o is ParadoxScriptProperty
        }

        override fun getEditorTitle(element: PsiElement?, additionalElements: MutableCollection<PsiElement>): String {
            return provider.presentableName
        }

        override fun getElementTitle(element: PsiElement): String? {
            ProgressManager.checkCanceled()
            return runReadAction { doGetElementTitle(element) }
        }

        private fun doGetElementTitle(element: PsiElement): String? {
            return when (element) {
                is PsiDirectory -> element.name
                is ParadoxScriptProperty -> ParadoxTechnologyManager.getName(element)
                else -> null
            }
        }

        override fun getNodeItems(nodeElement: PsiElement?, builder: DiagramBuilder): Array<Any> {
            ProgressManager.checkCanceled()
            return runReadAction { doGetNodeItems(nodeElement) }
        }

        private fun doGetNodeItems(nodeElement: PsiElement?): Array<Any> {
            return when (nodeElement) {
                is ParadoxScriptProperty -> {
                    val result = mutableListOf<Any>()
                    val typeElement = nodeElement.definitionInfo?.typeConfig?.pointer?.element //should not be null
                    if (typeElement != null) result.add(typeElement)
                    val properties = getProperties(nodeElement)
                    result.addAll(properties)
                    val localizedNameElement = ParadoxTechnologyManager.getLocalizedNameElement(nodeElement)
                    if (localizedNameElement != null) result.add(localizedNameElement)
                    val icon = ParadoxTechnologyManager.getIconFile(nodeElement)
                    if (icon != null) result.add(icon)
                    result.add(nodeElement)
                    result.toTypedArray()
                }
                else -> emptyArray()
            }
        }

        private fun getProperties(nodeElement: ParadoxScriptProperty): Set<ParadoxScriptProperty> {
            provider as ParadoxTechTreeDiagramProvider
            val itemPropertyKeys = provider.getItemPropertyKeys()
            val properties = sortedSetOf<ParadoxScriptProperty>(compareBy { itemPropertyKeys.indexOf(it.name.lowercase()) })
            nodeElement.block?.properties(conditional = true, inline = true)?.forEach {
                if (it.name.lowercase() in itemPropertyKeys) properties.add(it)
            }
            return properties
        }

        override fun getItemComponent(nodeElement: PsiElement, nodeItem: Any?, builder: DiagramBuilder): JComponent? {
            ProgressManager.checkCanceled()
            return runReadAction { doGetItemComponent(nodeElement, nodeItem) }
        }

        private fun doGetItemComponent(nodeElement: PsiElement, nodeItem: Any?): JComponent? {
            return when (nodeElement) {
                is ParadoxScriptProperty -> {
                    when {
                        nodeItem is ParadoxLocalisationProperty -> {
                            //科技的名字
                            ParadoxLocalisationTextUIRenderer().render(nodeItem)
                        }
                        nodeItem is PsiFile -> {
                            //科技的图标
                            val frameInfo = nodeElement.getUserData(PlsKeys.imageFrameInfo)
                            val iconUrl = ParadoxImageManager.resolveUrlByFile(nodeItem.virtualFile, nodeItem.project, frameInfo)

                            //如果无法解析（包括对应文件不存在的情况）就直接跳过
                            if (!ParadoxImageManager.canResolve(iconUrl)) return null

                            val iconFileUrl = iconUrl.toFileUrl()
                            val icon = iconFileUrl.toIconOrNull()
                            icon?.toLabel()
                        }
                        nodeItem is ParadoxScriptProperty -> {
                            //科技树
                            val definitionInfo = nodeItem.definitionInfo
                            if (definitionInfo == null) return null
                            val presentation = ParadoxDefinitionPresentationProvider.getPresentation(nodeItem, definitionInfo)
                            presentation
                        }
                        else -> null
                    }
                }
                else -> null
            }
        }

        override fun getItemName(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder): SimpleColoredText? {
            ProgressManager.checkCanceled()
            return runReadAction { doGetItemName(nodeElement, nodeItem) }
        }

        private fun doGetItemName(nodeElement: PsiElement?, nodeItem: Any?): SimpleColoredText? {
            return when (nodeElement) {
                is ParadoxScriptProperty -> {
                    when {
                        nodeItem is CwtProperty -> {
                            val typesText = nodeElement.definitionInfo?.typesText ?: return null
                            val result = SimpleColoredText(typesText, DEFAULT_TEXT_ATTR)
                            result
                        }
                        nodeItem is ParadoxScriptProperty -> {
                            val definitionInfo = nodeItem.definitionInfo
                            if (definitionInfo != null) return null
                            val rendered = ParadoxScriptTextRenderer(renderInBlock = true).render(nodeItem)
                            val result = SimpleColoredText(rendered, DEFAULT_TEXT_ATTR)
                            val propertyValue = nodeItem.propertyValue
                            if (propertyValue is ParadoxScriptScriptedVariableReference) {
                                val sv = propertyValue.text
                                result.append(" by $sv", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                            }
                            result
                        }
                        else -> null
                    }
                }
                else -> null
            }
        }

        override fun getItemType(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder?): SimpleColoredText? {
            return null
        }

        override fun getItemIcon(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder?): Icon? {
            ProgressManager.checkCanceled()
            return runReadAction { doGetItemIcon(nodeElement, nodeItem) }
        }

        private fun doGetItemIcon(nodeElement: PsiElement?, nodeItem: Any?): Icon? {
            return when (nodeElement) {
                is ParadoxScriptProperty -> {
                    when {
                        nodeItem is CwtProperty -> PlsIcons.Nodes.Type
                        nodeItem is ParadoxScriptProperty -> {
                            val definitionInfo = nodeItem.definitionInfo
                            if (definitionInfo != null) return null
                            PlsIcons.Nodes.Property
                        }
                        else -> null
                    }
                }
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
        override fun getModificationTracker(): FilePathBasedModificationTracker {
            return ParadoxModificationTrackers.ScriptFileTracker("common/technologies/**/*.txt")
        }

        override fun updateDataModel() {
            //群星原版科技有400+
            provider as ParadoxDefinitionDiagramProvider

            val technologies0 = getDefinitions(ParadoxDefinitionTypes.Technology)
            if (technologies0.isEmpty()) return
            val settings = provider.getDiagramSettings(project)?.state
            val technologies = technologies0.filter { settings == null || showNode(it, settings) }
            if (technologies.isEmpty()) return

            val nodeMap = mutableMapOf<ParadoxScriptDefinitionElement, Node>()
            val techMap = mutableMapOf<String, ParadoxScriptDefinitionElement>()
            for (technology in technologies) {
                ProgressManager.checkCanceled()
                val node = Node(technology, provider)
                node.putUserData(Keys.nodeData, technology.getData())
                nodeMap.put(technology, node)
                val name = technology.definitionInfo?.name.orAnonymous()
                techMap.put(name, technology)
                nodes.add(node)
            }
            for (technology in technologies) {
                ProgressManager.checkCanceled()
                val data = technology.getData<StellarisTechnologyData>() ?: continue
                //循环科技 ..> 循环科技
                val levels = data.levels
                if (levels != null) {
                    val label = if (levels <= 0) "max level: inf" else "max level: $levels"
                    val node = nodeMap.get(technology) ?: continue
                    val edge = Edge(node, node, REL_REPEAT(label))
                    edges.add(edge)
                }
                //前置 --> 科技
                val prerequisites = data.prerequisites
                if (prerequisites.isNotEmpty()) {
                    for (prerequisite in prerequisites) {
                        val source = techMap.get(prerequisite)?.let { nodeMap.get(it) } ?: continue
                        val target = nodeMap.get(technology) ?: continue
                        val edge = Edge(source, target, REL_PREREQUISITE)
                        edges.add(edge)
                    }
                }
            }
        }
    }
}
