package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.diagram.extras.custom.*
import com.intellij.diagram.presentation.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.graph.*
import com.intellij.openapi.graph.layout.*
import com.intellij.openapi.graph.settings.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.util.PlatformIcons
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.selectors.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import javax.swing.*

class ParadoxTechnologyTreeDiagramProvider : DiagramProvider<PsiElement>() {
    companion object {
        val CAT_PROPERTIES = DiagramCategory(PlsBundle.lazyMessage("diagram.technologyTree.category.properties"), PlatformIcons.PROPERTY_ICON, true, false)
        val CATEGORIES = arrayOf(CAT_PROPERTIES)
        //val CAT_TIER = DiagramCategory(PlsBundle.lazyMessage("diagram.technologyTree.category.tier"), PlatformIcons.PROPERTY_ICON, true, false)
        //val CAT_CATEGORY = DiagramCategory(PlsBundle.lazyMessage("diagram.technologyTree.category.category"), PlatformIcons.PROPERTY_ICON, true, false)
        //val CAT_AREA = DiagramCategory(PlsBundle.lazyMessage("diagram.technologyTree.category.area"), PlatformIcons.PROPERTY_ICON, true, false)
        //val CATEGORIES = arrayOf(CAT_TIER, CAT_CATEGORY, CAT_AREA)
        val ITEM_PROP_KEYS = arrayOf(
            "icon",
            "tier", "category", "area",
            "cost", "cost_per_level", "levels",
            "is_rare", "is_dangerous"
        )
        
        val REL_UNLOCK = object : DiagramRelationshipInfoAdapter("UNLOCK", DiagramLineType.SOLID, PlsBundle.message("diagram.technologyTree.rel.unlock")) {
            override fun getTargetArrow() = DELTA
        }
    }
    
    class NodeContentManager : AbstractDiagramNodeContentManager() {
        override fun isInCategory(nodeElement: Any?, item: Any?, category: DiagramCategory, builder: DiagramBuilder?): Boolean {
            //if(builder != null && nodeElement is ParadoxScriptProperty && nodeElement.definitionInfo?.type == "technology") {
            //    return when(category) {
            //        CAT_TIER -> item is ParadoxScriptProperty && item.name.equals("tier", true)
            //        CAT_CATEGORY -> item is ParadoxScriptProperty && item.name.equals("category", true)
            //        CAT_AREA -> item is ParadoxScriptProperty && item.name.equals("area", true)
            //        else -> null
            //    }
            //}
            //return false
            return true
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
            val rootFile = file.fileInfo?.rootInfo?.rootFile ?: return null
            return rootFile.toPsiDirectory(project)
        }
        
        override fun isAcceptableAsNode(o: Any?): Boolean {
            return o is PsiDirectory || o is ParadoxScriptProperty
        }
        
        override fun getEditorTitle(element: PsiElement?, additionalElements: MutableCollection<PsiElement>): String? {
            if(element == null) return null
            val gameType = selectGameType(element) ?: return null //unexpected
            return PlsBundle.message("diagram.technologyTree.editorTitle", gameType.description)
        }
        
        override fun getElementTitle(element: PsiElement): String? {
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
            return when(nodeElement) {
                is ParadoxScriptProperty -> {
                    val result = sortedSetOf<ParadoxScriptProperty>(compareBy { ITEM_PROP_KEYS.indexOf(it.name.lowercase()) })
                    nodeElement.block?.processProperty(conditional = true, inline = true) {
                        if(it.name.lowercase() in ITEM_PROP_KEYS) result.add(it)
                        true
                    }
                    result.toTypedArray()
                }
                else -> emptyArray()
            }
        }
        
        override fun getItemIcon(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder?): Icon {
            return PlsIcons.Property
        }
        
        override fun getItemName(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder): SimpleColoredText? {
            return when(nodeItem) {
                is ParadoxScriptProperty -> {
                    val name = nodeItem.name.lowercase()
                    val value = nodeItem.propertyValue?.stringValue()?.quoteIfNecessary()
                    val text = "$name = $value"
                    SimpleColoredText(text, DEFAULT_TEXT_ATTR)
                }
                else -> null
            }
        }
        
        override fun getItemType(nodeElement: PsiElement?, nodeItem: Any?, builder: DiagramBuilder?): SimpleColoredText? {
            return null
        }
    }
    
    class VfsResolver : DiagramVfsResolver<PsiElement> {
        //based on rootFile
        
        override fun getQualifiedName(element: PsiElement?): String? {
            if(element == null) return null
            val rootInfo = element.fileInfo?.rootInfo ?: return null
            val rootPath = rootInfo.rootFile.path
            return rootPath
        }
        
        override fun resolveElementByFQN(s: String, project: Project): PsiDirectory? {
            return try {
                s.toVirtualFile()?.toPsiDirectory(project)
            } catch(e: Exception) {
                null
            }
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

        override fun getCustomLayouter(settings: GraphSettings, project: Project?): Layouter {
            val layouter = GraphManager.getGraphManager().createHierarchicGroupLayouter()
            layouter.orientationLayouter = GraphManager.getGraphManager().createOrientationLayouter(LayoutOrientation.LEFT_TO_RIGHT)
            layouter.minimalNodeDistance = 20.0
            layouter.minimalLayerDistance = 50.0
            layouter.layerer = GraphManager.getGraphManager().createBFSLayerer()
            return layouter
        }
    }
    
    class Node(
        technology: ParadoxScriptProperty,
        provider: ParadoxTechnologyTreeDiagramProvider
    ) : PsiDiagramNode<PsiElement>(technology, provider) {
        override fun getTooltip(): String? {
            val element = identifyingElement
            if(element !is ParadoxScriptProperty) return null
            return ParadoxTechnologyHandler.getName(element)
        }
    }
    
    class Edge(
        source: Node,
        target: Node,
        relationship: DiagramRelationshipInfo = REL_UNLOCK
    ) : DiagramEdgeBase<PsiElement>(source, target, relationship)
    
    class DataModel(
        project: Project,
        val file: VirtualFile?, //umlFile
        val provider: ParadoxTechnologyTreeDiagramProvider
    ) : DiagramDataModel<PsiElement>(project, provider), ModificationTracker {
        val _nodes = mutableSetOf<DiagramNode<PsiElement>>()
        val _edges = mutableSetOf<DiagramEdge<PsiElement>>()
        
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
            _nodes.clear()
            _edges.clear()
            val originalFile = file?.getUserData(DiagramDataKeys.ORIGINAL_ELEMENT)
            val technologies = ParadoxTechnologyHandler.getTechnologies(project, originalFile)
            if(technologies.isEmpty()) return
            val nodeMap = mutableMapOf<ParadoxScriptProperty, Node>()
            val techMap = mutableMapOf<String, ParadoxScriptProperty>()
            for(technology in technologies) {
                val node = Node(technology, provider)
                nodeMap.put(technology, node)
                techMap.put(ParadoxTechnologyHandler.getName(technology), technology)
                _nodes.add(node)
            }
            for(technology in technologies) {
                val prerequisites = ParadoxTechnologyHandler.getPrerequisites(technology)
                if(prerequisites.isEmpty()) continue
                for(prerequisite in prerequisites) {
                    val source = nodeMap.get(technology) ?: continue
                    val target = techMap.get(prerequisite)?.let { nodeMap.get(it) } ?: continue
                    val edge = Edge(source, target)
                    _edges.add(edge)
                }
            }
        }
    }
    
    val _elementManager = ElementManager()
    val _vfsResolver = VfsResolver()
    val _relationshipManager = RelationshipManager()
    val _extras = Extras()
    
    init {
        _elementManager.setUmlProvider(this)
    }
    
    override fun getID() = "Paradox.TechTree"
    
    override fun createVisibilityManager() = emptyDiagramVisibilityManager
    
    override fun createNodeContentManager() = NodeContentManager()
    
    override fun getElementManager() = _elementManager
    
    override fun getVfsResolver() = _vfsResolver
    
    override fun getRelationshipManager() = _relationshipManager
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getExtras() = _extras
    
    override fun getAllContentCategories() = CATEGORIES
    
    override fun getActionName(isPopup: Boolean) = PlsBundle.message("diagram.technologyTree.actionName")
    
    override fun getPresentableName() = PlsBundle.message("diagram.technologyTree.name")
}