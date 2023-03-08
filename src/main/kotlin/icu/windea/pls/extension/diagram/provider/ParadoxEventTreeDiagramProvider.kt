package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.extras.custom.*
import com.intellij.diagram.presentation.*
import com.intellij.diagram.settings.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.graph.*
import com.intellij.openapi.graph.layout.*
import com.intellij.openapi.graph.settings.*
import com.intellij.openapi.graph.view.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.data.ParadoxEventDataProvider.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.localisation.*
import icu.windea.pls.tool.script.*
import java.awt.*
import javax.swing.*

/**
 * 提供事件树图表。
 * * 可以配置是否显示事件标题、图片、关键属性。
 * * 可以按类型过滤要显示的事件。
 * * TODO 可以按作用域过滤要显示的科技。（例如，仅限原版，仅限当前模组）
 * * 支持任何通用的图表操作。（例如，导出为图片）
 */
class ParadoxEventTreeDiagramProvider : ParadoxDiagramProvider() {
    val _vfsResolver = ParadoxRootVfsResolver(presentableName)
    val _elementManager = ElementManager()
    val _relationshipManager = RelationshipManager()
    val _extras = Extras()
    
    init {
        _elementManager.setUmlProvider(this)
    }
    
    override fun getID() = "Paradox.EventTree"
    
    override fun getPresentableName() = PlsDiagramBundle.message("paradox.eventTree.name")
    
    override fun getActionName(isPopup: Boolean) = PlsDiagramBundle.message("paradox.eventTree.actionName")
    
    override fun createScopeManager(project: Project) = null //TODO
    
    override fun createNodeContentManager() = NodeContentManager()
    
    override fun getVfsResolver() = _vfsResolver
    
    override fun getElementManager() = _elementManager
    
    override fun getRelationshipManager() = _relationshipManager
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getExtras() = _extras
    
    override fun getAllContentCategories() = CATEGORIES
    
    companion object {
        val CAT_TYPE = DiagramCategory(PlsDiagramBundle.lazyMessage("paradox.eventTree.category.type"), PlsIcons.Type, true, false)
        val CAT_PROPERTIES = DiagramCategory(PlsDiagramBundle.lazyMessage("paradox.eventTree.category.properties"), PlsIcons.Property, true, false)
        val CAT_TITLE = DiagramCategory(PlsDiagramBundle.lazyMessage("paradox.eventTree.category.title"), PlsIcons.Localisation, false, false)
        val CAT_PICTURE = DiagramCategory(PlsDiagramBundle.lazyMessage("paradox.eventTree.category.picture"), PlsIcons.Image, false, false)
        val CATEGORIES = arrayOf(CAT_TYPE, CAT_PROPERTIES, CAT_TITLE, CAT_PICTURE)
        val ITEM_PROP_KEYS = arrayOf(
            "picture",
            "hide_window", "is_triggered_only", "major", "diplomatic"
        )
        
        val REL_INVOKE = object : DiagramRelationshipInfoAdapter("INVOKE", DiagramLineType.SOLID) {
            override fun getTargetArrow() = DELTA
        }
        val REL_INVOKE_IMMEDIATE = object : DiagramRelationshipInfoAdapter("INVOKE_IMMEDIATE", DiagramLineType.SOLID, PlsDiagramBundle.message("paradox.eventTree.rel.invokeImmediate")) {
            override fun getTargetArrow() = DELTA
        }
        val REL_INVOKE_AFTER = object : DiagramRelationshipInfoAdapter("PREREQUISITE", DiagramLineType.SOLID, PlsDiagramBundle.message("paradox.eventTree.rel.invokeAfter")) {
            override fun getTargetArrow() = DELTA
        }
    }
    
    class NodeContentManager : OrderedDiagramNodeContentManager() {
        override fun isInCategory(nodeElement: Any?, item: Any?, category: DiagramCategory, builder: DiagramBuilder?): Boolean {
            return when {
                item is CwtProperty -> category == CAT_TYPE
                item is ParadoxScriptProperty -> {
                    val definitionInfo = item.definitionInfo
                    if(definitionInfo != null) {
                        false
                    } else {
                        category == CAT_PROPERTIES
                    }
                }
                item is ParadoxLocalisationProperty -> category == CAT_TITLE
                item is PsiFile -> category == CAT_PICTURE
                else -> false
            }
        }
        
        override fun getContentCategories(): Array<DiagramCategory> {
            return CATEGORIES
        }
    }
    
    class ElementManager : DiagramElementManagerEx<PsiElement>() {
        override fun findInDataContext(context: DataContext): PsiElement? {
            //rootFile
            val file = context.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val project = context.getData(CommonDataKeys.PROJECT) ?: return null
            val rootInfo = file.fileInfo?.rootInfo ?: return null
            val rootFile = rootInfo.rootFile
            return rootFile.toPsiDirectory(project)
        }
        
        override fun isAcceptableAsNode(o: Any?): Boolean {
            return o is PsiDirectory || o is ParadoxScriptProperty
        }
        
        override fun getEditorTitle(element: PsiElement?, additionalElements: MutableCollection<PsiElement>): String? {
            if(element == null) return null
            val gameType = selectGameType(element) ?: return null //unexpected
            return PlsDiagramBundle.message("paradox.eventTree.editorTitle", gameType.description)
        }
        
        override fun getElementTitle(element: PsiElement): String? {
            ProgressManager.checkCanceled()
            return when(element) {
                is PsiDirectory -> element.name
                is ParadoxScriptProperty -> ParadoxEventHandler.getName(element)
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
                    val name = ParadoxEventHandler.getLocalizedName(nodeElement)
                    if(name != null) result.add(name)
                    val icon = ParadoxEventHandler.getIconFile(nodeElement)
                    if(icon != null) result.add(icon)
                    result.toTypedArray()
                }
                else -> emptyArray()
            }
        }
        
        private fun getProperties(nodeElement: ParadoxScriptProperty): Set<ParadoxScriptProperty> {
            val properties = sortedSetOf<ParadoxScriptProperty>(compareBy { ITEM_PROP_KEYS.indexOf(it.name.lowercase()) })
            nodeElement.block?.processProperty(conditional = true, inline = true) {
                if(it.name.lowercase() in ITEM_PROP_KEYS) properties.add(it)
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
                            //事件标题
                            ParadoxLocalisationTextUIRender.render(nodeItem)
                        }
                        nodeItem is PsiFile -> {
                            //事件图片
                            val iconUrl = ParadoxDdsUrlResolver.resolveByFile(nodeItem.virtualFile, nodeElement.getUserData(PlsKeys.iconFrame) ?: 0)
                            if(iconUrl.isEmpty()) return null
                            val icon = IconLoader.findIcon(iconUrl.toFileUrl())
                            icon?.toLabel()
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
    
    class ColorManager : DiagramColorManagerBase() {
        override fun getEdgeColor(builder: DiagramBuilder, edge: DiagramEdge<*>): Color {
            if(edge !is Edge) return super.getEdgeColor(builder, edge)
            //基于调用类型
            return doGetEdgeColor(edge) ?: super.getEdgeColor(builder, edge)
        }
        
        private fun doGetEdgeColor(edge: Edge): Color? {
            val invocationType = edge.invocationType
            return when(invocationType) {
                ParadoxEventHandler.InvocationType.All -> null
                ParadoxEventHandler.InvocationType.Immediate -> Color.RED
                ParadoxEventHandler.InvocationType.After -> Color.BLUE
            }
        }
    }
    
    class Node(
        event: ParadoxScriptProperty,
        provider: ParadoxEventTreeDiagramProvider,
        val data: Data?
    ) : PsiDiagramNode<PsiElement>(event, provider) {
        override fun getTooltip(): String? {
            val element = identifyingElement
            if(element !is ParadoxScriptProperty) return null
            return ParadoxEventHandler.getName(element)
        }
    }
    
    class Edge(
        source: Node,
        target: Node,
        relationship: DiagramRelationshipInfo = REL_INVOKE,
        val invocationType: ParadoxEventHandler.InvocationType,
    ) : DiagramEdgeBase<PsiElement>(source, target, relationship)
    
    class DataModel(
        project: Project,
        val file: VirtualFile?, //umlFile
        val provider: ParadoxEventTreeDiagramProvider
    ) : DiagramDataModel<PsiElement>(project, provider), ModificationTracker {
        val _nodes = mutableSetOf<DiagramNode<PsiElement>>()
        val _edges = mutableSetOf<DiagramEdge<PsiElement>>()
        
        override fun getNodes() = _nodes
        
        override fun getEdges() = _edges
        
        override fun getNodeName(node: DiagramNode<PsiElement>) = node.tooltip.orAnonymous()
        
        override fun addElement(element: PsiElement?) = null
        
        override fun getModificationTracker() = this
        
        override fun getModificationCount(): Long {
            return ParadoxModificationTrackerProvider.getInstance().Events.modificationCount
        }
        
        override fun dispose() {
            
        }
        
        override fun refreshDataModel() {
            doRefreshDataModel()
        }
        
        private fun doRefreshDataModel() {
            ProgressManager.checkCanceled()
            val settings = provider.extras.additionalDiagramSettings
            val configuration = DiagramConfiguration.getInstance()
            _nodes.clear()
            _edges.clear()
            val originalFile = file?.getUserData(DiagramDataKeys.ORIGINAL_ELEMENT)
            val events = ParadoxEventHandler.getEvents(project, originalFile)
            if(events.isEmpty()) return
            //群星原版事件有5000+
            val nodeMap = mutableMapOf<ParadoxScriptProperty, Node>()
            val eventMap = mutableMapOf<String, ParadoxScriptProperty>()
            for(event in events) {
                ProgressManager.checkCanceled()
                val data = event.getData<Data>()
                if(data != null && !shouldShow(data, settings, configuration)) continue
                val node = Node(event, provider, data)
                nodeMap.put(event, node)
                eventMap.put(ParadoxEventHandler.getName(event), event)
                _nodes.add(node)
            }
            for(event in events) {
                ProgressManager.checkCanceled()
                val invocations = ParadoxEventHandler.getInvocations(event)
                if(invocations.isEmpty()) continue
                //事件 --> 调用的事件
                for((invocation, invocationType) in invocations) {
                    ProgressManager.checkCanceled()
                    val source = nodeMap.get(event) ?: continue
                    val target = eventMap.get(invocation)?.let { nodeMap.get(it) } ?: continue
                    val relationship = when(invocationType) {
                        ParadoxEventHandler.InvocationType.All -> REL_INVOKE
                        ParadoxEventHandler.InvocationType.Immediate -> REL_INVOKE_IMMEDIATE
                        ParadoxEventHandler.InvocationType.After -> REL_INVOKE_AFTER
                    }
                    val edge = Edge(source, target, relationship, invocationType)
                    _edges.add(edge)
                }
            }
        }
        
        private fun shouldShow(data: Data, settings: Array<out DiagramConfigGroup>, configuration: DiagramConfiguration): Boolean {
            for(setting in settings) {
                when(setting.name) {
                    PlsDiagramBundle.message("paradox.eventTree.settings.type") -> {
                        val hidden = data.hide_window
                        val triggered = data.is_triggered_only
                        val major = data.major
                        val diplomatic = data.diplomatic
                        val other = !hidden && !triggered && !major && !diplomatic
                        val enabled = setting.elements.any { config ->
                            val e = configuration.isEnabledByDefault(provider, config.name)
                            when(config.name) {
                                PlsDiagramBundle.message("paradox.eventTree.settings.type.hidden") -> if(hidden) e else false
                                PlsDiagramBundle.message("paradox.eventTree.settings.type.triggered") -> if(triggered) e else false
                                PlsDiagramBundle.message("paradox.eventTree.settings.type.major") -> if(major) e else false
                                PlsDiagramBundle.message("paradox.eventTree.settings.type.diplomatic") -> if(diplomatic) e else false
                                PlsDiagramBundle.message("paradox.eventTree.settings.type.other") -> if(other) e else false
                                else -> false
                            }
                        }
                        if(!enabled) return false
                    }
                }
            }
            return true
        }
    }
    
    //class Extras : DiagramExtras<PsiElement>()
    
    class Extras : DiagramExtrasEx() {
        override fun getCustomLayouter(settings: GraphSettings, project: Project?): Layouter {
            val layouter = GraphManager.getGraphManager().createHierarchicGroupLayouter()
            layouter.orientationLayouter = GraphManager.getGraphManager().createOrientationLayouter(LayoutOrientation.LEFT_TO_RIGHT)
            layouter.layerer = GraphManager.getGraphManager().createBFSLayerer()
            layouter.minimalNodeDistance = 20.0
            layouter.minimalEdgeDistance = 40.0
            return layouter
        }
        
        override fun getAdditionalDiagramSettings(): Array<out DiagramConfigGroup> {
            val settings = buildList {
                DiagramConfigGroup(PlsDiagramBundle.message("paradox.eventTree.settings.type")).apply {
                    addElement(DiagramConfigElement(PlsDiagramBundle.message("paradox.eventTree.settings.type.hidden"), true))
                    addElement(DiagramConfigElement(PlsDiagramBundle.message("paradox.eventTree.settings.type.triggered"), true))
                    addElement(DiagramConfigElement(PlsDiagramBundle.message("paradox.eventTree.settings.type.major"), true))
                    addElement(DiagramConfigElement(PlsDiagramBundle.message("paradox.eventTree.settings.type.diplomatic"), true))
                    addElement(DiagramConfigElement(PlsDiagramBundle.message("paradox.eventTree.settings.type.other"), true))
                }.also { add(it) }
            }
            return settings.toTypedArray()
        }
    }
}