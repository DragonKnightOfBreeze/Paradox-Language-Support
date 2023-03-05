package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.diagram.extras.custom.*
import com.intellij.diagram.presentation.*
import com.intellij.diagram.settings.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
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
import com.intellij.util.concurrency.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.selectors.*
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.presentation.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.localisation.*
import icu.windea.pls.tool.script.*
import java.awt.*
import java.util.concurrent.*
import javax.swing.*

/**
 * 提供事件树图表。
 * * 可以配置是否显示事件标题、图片、关键属性。
 * * 可以按类型过滤要显示的事件。
 * * TODO 可以按作用域过滤要显示的科技。（例如，仅限原版，仅限当前模组）
 * * 支持任何通用的图表操作。（例如，导出为图片）
 */
class ParadoxEventTreeDiagramProvider : ParadoxDiagramProvider() {
    val _elementManager = ElementManager()
    val _relationshipManager = RelationshipManager()
    val _extras = Extras()
    
    init {
        _elementManager.setUmlProvider(this)
    }
    
    override fun getID() = "Paradox.EventTree"
    
    override fun getPresentableName() = PlsBundle.message("diagram.paradox.eventTree.name")
    
    override fun getActionName(isPopup: Boolean) = PlsBundle.message("diagram.paradox.eventTree.actionName")
    
    override fun createScopeManager(project: Project) = null //TODO
    
    override fun createNodeContentManager() = NodeContentManager()
    
    override fun getElementManager() = _elementManager
    
    override fun getRelationshipManager() = _relationshipManager
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getExtras() = _extras
    
    override fun getAllContentCategories() = CATEGORIES
    
    companion object {
        val CAT_TYPE = DiagramCategory(PlsBundle.lazyMessage("diagram.paradox.eventTree.category.type"), PlsIcons.Type, true, false)
        val CAT_PROPERTIES = DiagramCategory(PlsBundle.lazyMessage("diagram.paradox.eventTree.category.properties"), PlsIcons.Property, true, false)
        val CAT_TITLE = DiagramCategory(PlsBundle.lazyMessage("diagram.paradox.eventTree.category.title"), PlsIcons.Localisation, false, false)
        val CAT_PICTURE = DiagramCategory(PlsBundle.lazyMessage("diagram.paradox.eventTree.category.picture"), PlsIcons.Image, false, false)
        val CATEGORIES = arrayOf(CAT_TYPE, CAT_PROPERTIES, CAT_TITLE, CAT_PICTURE)
        val ITEM_PROP_KEYS = arrayOf(
            "picture",
            "hide_window", "is_triggered_only", "major", "diplomatic"
        )
        
        val REL_INVOKE = object : DiagramRelationshipInfoAdapter("INVOKE", DiagramLineType.SOLID) {
            override fun getTargetArrow() = DELTA
        }
        val REL_INVOKE_IMMEDIATE = object : DiagramRelationshipInfoAdapter("INVOKE_IMMEDIATE", DiagramLineType.SOLID, PlsBundle.message("diagram.paradox.eventTree.rel.invokeImmediate")) {
            override fun getTargetArrow() = DELTA
        }
        val REL_INVOKE_AFTER = object : DiagramRelationshipInfoAdapter("PREREQUISITE", DiagramLineType.SOLID, PlsBundle.message("diagram.paradox.eventTree.rel.invokeAfter")) {
            override fun getTargetArrow() = DELTA
        }
    }
    
    class NodeContentManager : AbstractDiagramNodeContentManager() {
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
    
    class ElementManager : AbstractDiagramElementManager<PsiElement>() {
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
            return PlsBundle.message("diagram.paradox.eventTree.editorTitle", gameType.description)
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
                    val typeElement = nodeElement.definitionInfo?.typeConfig //should not be null
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
                        nodeItem is ParadoxLocalisationProperty -> {
                            ParadoxLocalisationTextUIRender.renderImage(nodeItem)?.toIcon()
                        }
                        nodeItem is PsiFile -> {
                            val iconUrl = ParadoxDdsUrlResolver.resolveByFile(nodeItem.virtualFile, nodeElement.getUserData(PlsKeys.iconFrame) ?: 0)
                            if(iconUrl.isNotEmpty()) {
                                IconLoader.findIcon(iconUrl.toFileUrl())
                            } else {
                                null
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
    
    //class Extras : DiagramExtras<PsiElement>()
    
    class Extras : CommonDiagramExtras<PsiElement>() {
        //com.intellij.diagram.extras.DiagramExtras.getCustomLayouter
        
        override fun createNodeComponent(node: DiagramNode<PsiElement>, builder: DiagramBuilder, nodeRealizer: NodeRealizer, wrapper: JPanel): JComponent {
            return runReadAction {
                super.createNodeComponent(node, builder, nodeRealizer, wrapper)
            }
        }
        
        override fun getAdditionalDiagramSettings(): Array<out DiagramConfigGroup> {
            val settings = buildList {
                DiagramConfigGroup(PlsBundle.message("diagram.paradox.eventTree.settings.type")).apply {
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.paradox.eventTree.settings.type.hidden"), true))
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.paradox.eventTree.settings.type.triggered"), true))
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.paradox.eventTree.settings.type.major"), true))
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.paradox.eventTree.settings.type.diplomatic"), true))
                }.also { add(it) }
            }
            return settings.toTypedArray()
        }
        
        override fun getCustomLayouter(settings: GraphSettings, project: Project?): Layouter {
            val layouter = GraphManager.getGraphManager().createHierarchicGroupLayouter()
            layouter.orientationLayouter = GraphManager.getGraphManager().createOrientationLayouter(LayoutOrientation.LEFT_TO_RIGHT)
            layouter.minimalNodeDistance = 40.0
            layouter.minimalEdgeDistance = 40.0
            layouter.layerer = GraphManager.getGraphManager().createBFSLayerer()
            return layouter
        }
    }
    
    class Node(
        event: ParadoxScriptProperty,
        provider: ParadoxEventTreeDiagramProvider
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
        private val anchorColor: Color? = null,
    ) : DiagramEdgeBase<PsiElement>(source, target, relationship) {
        override fun getAnchorColor() = anchorColor
    }
    
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
                if(!shouldShow(event, settings, configuration)) continue
                val node = Node(event, provider)
                nodeMap.put(event, node)
                eventMap.put(ParadoxEventHandler.getName(event), event)
                _nodes.add(node)
            }
            for(event in events) {
                ProgressManager.checkCanceled()
                val invocations = ParadoxEventHandler.getInvocations(event)
                if(invocations.isEmpty()) continue
                for((invocation, invocationType) in invocations) {
                    ProgressManager.checkCanceled()
                    val source = nodeMap.get(event) ?: continue
                    val target = eventMap.get(invocation)?.let { nodeMap.get(it) } ?: continue
                    val relationship = when(invocationType) {
                        ParadoxEventHandler.InvocationType.All -> REL_INVOKE
                        ParadoxEventHandler.InvocationType.Immediate -> REL_INVOKE_IMMEDIATE
                        ParadoxEventHandler.InvocationType.After -> REL_INVOKE_AFTER
                    }
                    val anchorColor = when(invocationType) {
                        ParadoxEventHandler.InvocationType.All -> null
                        ParadoxEventHandler.InvocationType.Immediate -> Color.RED
                        ParadoxEventHandler.InvocationType.After -> Color.BLUE
                    }
                    val edge = Edge(source, target, relationship, anchorColor)
                    _edges.add(edge)
                }
            }
        }
        
        private fun shouldShow(event: ParadoxScriptProperty, settings: Array<out DiagramConfigGroup>, configuration: DiagramConfiguration): Boolean {
            val data = event.getData<ParadoxEventDataProvider.Data>() ?: return true
            for(setting in settings) {
                when(setting.name) {
                    PlsBundle.message("diagram.paradox.eventTree.settings.type") -> {
                        for(config in setting.elements) {
                            val enabled = configuration.isEnabledByDefault(provider, config.name)
                            if(enabled) continue
                            when(config.name) {
                                PlsBundle.message("diagram.paradox.eventTree.settings.type.hidden") -> {
                                    if(data.hide_window) return false
                                }
                                PlsBundle.message("diagram.paradox.eventTree.settings.type.triggered") -> {
                                    if(data.is_triggered_only) return false
                                }
                                PlsBundle.message("diagram.paradox.eventTree.settings.type.major") -> {
                                    if(data.major) return false
                                }
                                PlsBundle.message("diagram.paradox.eventTree.settings.type.diplomatic") -> {
                                    if(data.diplomatic) return false
                                }
                            }
                        }
                    }
                }
            }
            return true
        }
    }
}