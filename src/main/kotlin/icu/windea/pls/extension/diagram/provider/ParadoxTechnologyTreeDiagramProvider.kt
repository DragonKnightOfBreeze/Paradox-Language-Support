package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.presentation.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.ui.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.extras.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.presentation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.localisation.*
import icu.windea.pls.tool.script.*
import java.util.concurrent.*
import javax.swing.*

/**
 * 提供群星的科技树图表。
 * * 可以配置是否显示UI表示（科技卡图片）、科技名、图标、关键属性。
 * * 可以按类型、级别、分类、领域过滤要显示的科技。
 * * TODO 可以按作用域过滤要显示的科技。（例如，仅限原版，仅限当前模组）
 * * 支持任何通用的图表操作。（例如，导出为图片）
 */
abstract class ParadoxTechnologyTreeDiagramProvider(gameType: ParadoxGameType) : ParadoxDefinitionDiagramProvider(gameType) {
    companion object {
        val CAT_TYPE = DiagramCategory(PlsDiagramBundle.lazyMessage("paradox.technologyTree.category.type"), PlsIcons.Type, true, false)
        val CAT_PROPERTIES = DiagramCategory(PlsDiagramBundle.lazyMessage("paradox.technologyTree.category.properties"), PlsIcons.Property, true, false)
        val CAT_NAME = DiagramCategory(PlsDiagramBundle.lazyMessage("paradox.technologyTree.category.name"), PlsIcons.Localisation, false, false)
        val CAT_ICON = DiagramCategory(PlsDiagramBundle.lazyMessage("paradox.technologyTree.category.icon"), PlsIcons.Image, false, false)
        val CAT_PRESENTATION = DiagramCategory(PlsDiagramBundle.lazyMessage("paradox.technologyTree.category.presentation"), PlsIcons.Presentation, false, false)
        val CATEGORIES = arrayOf(CAT_TYPE, CAT_PROPERTIES, CAT_NAME, CAT_ICON, CAT_PRESENTATION)
        val ITEM_PROP_KEYS = arrayOf(
            "icon",
            "tier", "area", "category",
            "cost", "cost_per_level", "levels",
            "start_tech", "is_rare", "is_dangerous"
        )
        
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
    
    private val _vfsResolver = ParadoxRootVfsResolver()
    private val _elementManager = ElementManager(this)
    private val _relationshipManager = RelationshipManager()
    private val _colorManager = ColorManager()
    private val _extras = Extras(this)
    
    override fun getID() = gameType.name + ".TechnologyTree"
    
    override fun getPresentableName() = PlsDiagramBundle.message("paradox.technologyTree.name", gameType)
    
    override fun createNodeContentManager() = NodeContentManager()
    
    override fun getVfsResolver() = _vfsResolver
    
    override fun getElementManager() = _elementManager
    
    override fun getRelationshipManager() = _relationshipManager
    
    override fun getColorManager() = _colorManager
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getExtras() = _extras
    
    override fun getAllContentCategories() = CATEGORIES
    
    override abstract fun getDiagramSettings(): ParadoxTechlonogyTreeDiagramSettings<*>?
    
    class NodeContentManager : OrderedDiagramNodeContentManager() {
        override fun isInCategory(nodeElement: Any?, item: Any?, category: DiagramCategory, builder: DiagramBuilder?): Boolean {
            return when {
                item is CwtProperty -> category == ParadoxEventTreeDiagramProvider.CAT_TYPE
                item is ParadoxScriptProperty -> {
                    val definitionInfo = item.definitionInfo
                    if(definitionInfo != null) {
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
        
        override fun getEditorTitle(element: PsiElement?, additionalElements: MutableCollection<PsiElement>): String? {
            if(element == null) return null
            val gameType = selectGameType(element) ?: return null //unexpected
            return PlsDiagramBundle.message("paradox.technologyTree.editorTitle", gameType.description)
        }
        
        override fun getElementTitle(element: PsiElement): String? {
            ProgressManager.checkCanceled()
            return when(element) {
                is PsiDirectory -> element.name
                is ParadoxScriptProperty -> ParadoxTechnologyHandler.getName(element)
                else -> null
            }
        }
        
        override fun getNodeTooltip(element: PsiElement?): String? {
            return null
        }
        
        override fun getNodeItems(nodeElement: PsiElement?, builder: DiagramBuilder): Array<Any> {
            ProgressManager.checkCanceled()
            return when(nodeElement) {
                is ParadoxScriptProperty -> {
                    val result = mutableListOf<Any>()
                    val typeElement = nodeElement.definitionInfo?.typeConfig?.pointer?.element //should not be null
                    if(typeElement != null) result.add(typeElement)
                    val properties = getProperties(nodeElement)
                    result.addAll(properties)
                    val name = ParadoxTechnologyHandler.getLocalizedName(nodeElement)
                    if(name != null) result.add(name)
                    val icon = ParadoxTechnologyHandler.getIconFile(nodeElement)
                    if(icon != null) result.add(icon)
                    result.add(nodeElement)
                    result.toTypedArray()
                }
                else -> emptyArray()
            }
        }
        
        private fun getProperties(nodeElement: ParadoxScriptProperty): Set<ParadoxScriptProperty> {
            provider as ParadoxTechnologyTreeDiagramProvider
            val itemPropertyKeys = provider.getItemPropertyKeys()
            val properties = sortedSetOf<ParadoxScriptProperty>(compareBy { itemPropertyKeys.indexOf(it.name.lowercase()) })
            nodeElement.block?.processProperty(conditional = true, inline = true) {
                if(it.name.lowercase() in itemPropertyKeys) properties.add(it)
                true
            }
            return properties
        }
        
        override fun getItemComponent(nodeElement: PsiElement, nodeItem: Any?, builder: DiagramBuilder): JComponent? {
            ProgressManager.checkCanceled()
            return when(nodeElement) {
                is ParadoxScriptProperty -> {
                    when {
                        nodeItem is ParadoxLocalisationProperty -> {
                            //科技的名字
                            ParadoxLocalisationTextUIRender.render(nodeItem)
                        }
                        nodeItem is PsiFile -> {
                            //科技的图标
                            val iconUrl = ParadoxDdsUrlResolver.resolveByFile(nodeItem.virtualFile, nodeElement.getUserData(PlsKeys.iconFrameKey) ?: 0)
                            if(iconUrl.isEmpty()) return null
                            val icon = IconLoader.findIcon(iconUrl.toFileUrl())
                            icon?.toLabel()
                        }
                        nodeItem is ParadoxScriptProperty -> {
                            //科技树
                            val definitionInfo = nodeItem.definitionInfo
                            if(definitionInfo == null) return null
                            val presentation = ParadoxDefinitionPresentationProvider.getPresentation(nodeItem, definitionInfo)
                            presentation
                        }
                        else -> null
                    }
                }
                else -> null
            }
        }
        
        override fun getItemIcon(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder?): Icon? {
            ProgressManager.checkCanceled()
            return when(nodeElement) {
                is ParadoxScriptProperty -> {
                    when {
                        nodeItem is CwtProperty -> PlsIcons.Type
                        nodeItem is ParadoxScriptProperty -> {
                            val definitionInfo = nodeItem.definitionInfo
                            if(definitionInfo != null) {
                                null
                            } else {
                                PlsIcons.Property
                            }
                        }
                        else -> null
                    }
                }
                else -> null
            }
        }
        
        override fun getItemName(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder): SimpleColoredText? {
            ProgressManager.checkCanceled()
            return when(nodeElement) {
                is ParadoxScriptProperty -> {
                    when {
                        nodeItem is CwtProperty -> {
                            val typesText = nodeElement.definitionInfo?.typesText ?: return null
                            val result = SimpleColoredText(typesText, DEFAULT_TEXT_ATTR)
                            result
                        }
                        nodeItem is ParadoxScriptProperty -> {
                            val definitionInfo = nodeItem.definitionInfo
                            if(definitionInfo != null) {
                                null
                            } else {
                                val rendered = ParadoxScriptTextRender.render(nodeItem, renderInBlock = true)
                                val result = SimpleColoredText(rendered, DEFAULT_TEXT_ATTR)
                                val propertyValue = nodeItem.propertyValue
                                if(propertyValue is ParadoxScriptScriptedVariableReference) {
                                    val sv = propertyValue.text
                                    result.append(" by $sv", SimpleTextAttributes.GRAYED_ATTRIBUTES)
                                }
                                result
                            }
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
        
        @Suppress("RedundantOverride")
        override fun getItemDocOwner(element: Any?, builder: DiagramBuilder): PsiElement? {
            //property -> No documentation found -> ok
            return super.getItemDocOwner(element, builder)
        }
    }
    
    class RelationshipManager : DiagramRelationshipManager<PsiElement> {
        override fun getDependencyInfo(s: PsiElement?, t: PsiElement?, category: DiagramCategory?): DiagramRelationshipInfo? {
            return null
        }
    }
    
    open class ColorManager : DiagramColorManagerBase()
    
    class Node(
        element: ParadoxScriptProperty,
        provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxDefinitionDiagramNode(element, provider) 
    
    class Edge(
        source: Node,
        target: Node,
        relationship: DiagramRelationshipInfo
    ) : ParadoxDefinitionDiagramEdge(source, target, relationship)
    
    class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxTechnologyTreeDiagramProvider
    ) : ParadoxDiagramDataModel(project, file, provider) {
        private val _nodes = mutableSetOf<DiagramNode<PsiElement>>()
        private val _edges = mutableSetOf<DiagramEdge<PsiElement>>()
        
        override fun getNodes() = _nodes
        
        override fun getEdges() = _edges
        
        override fun getNodeName(node: DiagramNode<PsiElement>) = node.tooltip.orAnonymous()
        
        override fun addElement(element: PsiElement?) = null
        
        override fun getModificationTracker() = this
        
        override fun getModificationCount(): Long {
            return ParadoxModificationTrackerProvider.getInstance().Technologies.modificationCount
        }
        
        override fun dispose() {
            
        }
        
        override fun refreshDataModel() {
            doRefreshDataModel()
        }
        
        private fun doRefreshDataModel() {
            provider as ParadoxTechnologyTreeDiagramProvider
            
            ProgressManager.checkCanceled()
            _nodes.clear()
            _edges.clear()
            val searchScope = scopeManager?.currentScope?.let { GlobalSearchScopes.filterScope(project, it) }
            val searchScopeType = provider.getDiagramSettings()?.state?.scopeType
            val selector = definitionSelector(project, originalFile)
                .withGameType(gameType)
                .withSearchScope(searchScope)
                .withSearchScopeType(searchScopeType)
                .contextSensitive()
                .distinctByName()
            val technologies = ParadoxTechnologyHandler.getTechnologies(selector)
            if(technologies.isEmpty()) return
            //群星原版科技有400+
            val nodeMap = mutableMapOf<ParadoxScriptProperty, Node>()
            val techMap = mutableMapOf<String, ParadoxScriptProperty>()
            for(technology in technologies) {
                ProgressManager.checkCanceled()
                if(!provider.showNode(technology)) continue
                val node = Node(technology, provider)
                provider.handleNode(node)
                nodeMap.put(technology, node)
                techMap.put(ParadoxTechnologyHandler.getName(technology), technology)
                _nodes.add(node)
            }
            for(technology in technologies) {
                ProgressManager.checkCanceled()
                val data = technology.getData<StellarisTechnologyDataProvider.Data>() ?: continue
                //循环科技 ..> 循环科技
                val levels = data.levels
                if(levels != null) {
                    val label = if(levels <= 0) "max level: inf" else "max level: $levels"
                    val node = nodeMap.get(technology) ?: continue
                    val edge = Edge(node, node, REL_REPEAT(label))
                    _edges.add(edge)
                }
                //前置 --> 科技
                val prerequisites = data.prerequisites
                if(prerequisites.isNotEmpty()) {
                    for(prerequisite in prerequisites) {
                        val source = techMap.get(prerequisite)?.let { nodeMap.get(it) } ?: continue
                        val target = nodeMap.get(technology) ?: continue
                        val edge = Edge(source, target, REL_PREREQUISITE)
                        _edges.add(edge)
                    }
                }
            }
        }
    }
    
    class Extras(provider: ParadoxDiagramProvider) : ParadoxDiagramExtras(provider)
}