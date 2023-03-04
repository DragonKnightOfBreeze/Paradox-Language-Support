package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.diagram.extras.custom.*
import com.intellij.diagram.presentation.*
import com.intellij.diagram.settings.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.graph.*
import com.intellij.openapi.graph.layout.*
import com.intellij.openapi.graph.settings.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.selectors.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.presentation.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.tool.*
import icu.windea.pls.tool.script.*
import javax.swing.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyTreeDiagramProvider : ParadoxDiagramProvider() {
    val _elementManager = ElementManager()
    val _relationshipManager = RelationshipManager()
    val _extras = Extras()
    
    init {
        _elementManager.setUmlProvider(this)
    }
    
    override fun getID() = "Stellaris.TechnologyTree"
    
    override fun getPresentableName() = PlsBundle.message("diagram.stellaris.technologyTree.name")
    
    override fun getActionName(isPopup: Boolean) = PlsBundle.message("diagram.stellaris.technologyTree.actionName")
    
    override fun createNodeContentManager() = NodeContentManager()
    
    override fun getElementManager() = _elementManager
    
    override fun getRelationshipManager() = _relationshipManager
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getExtras() = _extras
    
    override fun createScopeManager(project: Project) = DiagramPsiScopeManager<PsiElement>(project)
    
    override fun getAllContentCategories() = CATEGORIES
    
    companion object {
        val CAT_PRESENTATION = DiagramCategory(PlsBundle.lazyMessage("diagram.stellaris.technologyTree.category.presentation"), PlsIcons.Image, true, false)
        val CAT_ICON = DiagramCategory(PlsBundle.lazyMessage("diagram.stellaris.technologyTree.category.icon"), PlsIcons.Image, false, false)
        val CAT_PROPERTIES = DiagramCategory(PlsBundle.lazyMessage("diagram.stellaris.technologyTree.category.properties"), PlsIcons.Property, false, false)
        val CATEGORIES = arrayOf(CAT_PRESENTATION, CAT_ICON, CAT_PROPERTIES)
        val ITEM_PROP_KEYS = arrayOf(
            "icon",
            "tier", "category", "area",
            "cost", "cost_per_level", "levels",
            "start_tech", "is_rare", "is_dangerous"
        )
        
        val REL_PREREQUISITE = object : DiagramRelationshipInfoAdapter("PREREQUISITE", DiagramLineType.SOLID, PlsBundle.message("diagram.stellaris.technologyTree.rel.prerequisite")) {
            override fun getTargetArrow() = DELTA
        }
    }
    
    class NodeContentManager : AbstractDiagramNodeContentManager() {
        override fun isInCategory(nodeElement: Any?, item: Any?, category: DiagramCategory, builder: DiagramBuilder?): Boolean {
            return when {
                item is ParadoxScriptProperty -> {
                    val definitionInfo = item.definitionInfo
                    if(definitionInfo != null) {
                        category == CAT_PRESENTATION
                    } else {
                        category == CAT_PROPERTIES
                    }
                }
                item is PsiFile -> category == CAT_ICON
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
            if(rootInfo.gameType != ParadoxGameType.Stellaris) return null
            val rootFile = rootInfo.rootFile
            return rootFile.toPsiDirectory(project)
        }
        
        override fun isAcceptableAsNode(o: Any?): Boolean {
            return o is PsiDirectory || o is ParadoxScriptProperty
        }
        
        override fun getEditorTitle(element: PsiElement?, additionalElements: MutableCollection<PsiElement>): String? {
            if(element == null) return null
            val gameType = selectGameType(element) ?: return null //unexpected
            return PlsBundle.message("diagram.stellaris.technologyTree.editorTitle", gameType.description)
        }
        
        override fun getElementTitle(element: PsiElement): String? {
            return when(element) {
                is PsiDirectory -> element.name
                is ParadoxScriptProperty -> StellarisTechnologyHandler.getName(element)
                else -> null
            }
        }
        
        override fun getNodeTooltip(element: PsiElement?): String? {
            return null
        }
        
        override fun getNodeItems(nodeElement: PsiElement?, builder: DiagramBuilder): Array<Any> {
            return when(nodeElement) {
                is ParadoxScriptProperty -> {
                    val result = mutableListOf<Any>()
                    result.add(nodeElement)
                    val icon = StellarisTechnologyHandler.getIconFile(nodeElement)
                    if(icon != null) result.add(icon)
                    val properties = getProperties(nodeElement)
                    result.addAll(properties)
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
            return when(nodeElement) {
                is ParadoxScriptProperty -> {
                    when {
                        nodeItem is ParadoxScriptProperty -> {
                            val definitionInfo = nodeItem.definitionInfo
                            if(definitionInfo != null) {
                                val presentation = ParadoxDefinitionPresentationProvider.EP_NAME.extensionList
                                    .findIsInstance<StellarisTechnologyPresentationProvider>()
                                    ?.getPresentation(nodeItem, nodeItem.definitionInfo!!)
                                presentation?.let { IconUtil.createImageIcon(it) }
                            } else {
                                PlsIcons.Property
                            }
                        }
                        nodeItem is PsiFile -> {
                            val url = ParadoxDdsUrlResolver.resolveByFile(nodeItem.virtualFile, nodeElement.getUserData(PlsKeys.iconFrame) ?: 0)
                            if(url.isNotEmpty()) {
                                IconLoader.findIcon(url.toFileUrl())
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
            return when(nodeElement) {
                is ParadoxScriptProperty -> {
                    when {
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
    }
    
    class RelationshipManager : DiagramRelationshipManager<PsiElement> {
        override fun getDependencyInfo(s: PsiElement?, t: PsiElement?, category: DiagramCategory?): DiagramRelationshipInfo? {
            return null
        }
    }
    
    //class Extras : DiagramExtras<PsiElement>()
    
    class Extras : CommonDiagramExtras<PsiElement>() {
        //com.intellij.diagram.extras.DiagramExtras.getCustomLayouter
        
        override fun getAdditionalDiagramSettings(): Array<out DiagramConfigGroup> {
            val settings  = buildList {
                DiagramConfigGroup(PlsBundle.message("diagram.stellaris.technologyTree.settings.type")).apply {
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.type.start"), true))
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.type.rare"), true))
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.type.dangerous"), true))
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.type.repeatable"), true))
                }.also { add(it) }
                //tier和category应当是动态获取的，并且设置名不能包含本地化名字，因为这里的设置名同时也作为设置的ID
                //这里我们无法获得project，因此暂且合并所有已打开的项目
                ProgressManager.checkCanceled()
                DiagramConfigGroup(PlsBundle.message("diagram.stellaris.technologyTree.settings.tier")).apply {
                    val tiers = ProjectManager.getInstance().openProjects.flatMap { project ->
                        StellarisTechnologyHandler.getTechnologyTiers(project, null)
                    }
                    tiers.forEach {
                        addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.tier.option", it.name), true))
                    }
                }.also { add(it) }
                ProgressManager.checkCanceled()
                DiagramConfigGroup(PlsBundle.message("diagram.stellaris.technologyTree.settings.area")).apply {
                    val areas = StellarisTechnologyHandler.getResearchAreas()
                    areas.forEach {
                        addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.area.option", it), true))
                    }
                }.also { add(it) }
                ProgressManager.checkCanceled()
                DiagramConfigGroup(PlsBundle.message("diagram.stellaris.technologyTree.settings.category")).apply {
                    val categories = ProjectManager.getInstance().openProjects.flatMap { project ->
                        StellarisTechnologyHandler.getTechnologyCategories(project, null)
                    }
                    categories.forEach {
                        addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.category.option", it.name), true))
                    }
                }.also { add(it) }
            }
            return settings.toTypedArray()
        }
        
        override fun getCustomLayouter(settings: GraphSettings, project: Project?): Layouter {
            val layouter = GraphManager.getGraphManager().createHierarchicGroupLayouter()
            layouter.orientationLayouter = GraphManager.getGraphManager().createOrientationLayouter(LayoutOrientation.LEFT_TO_RIGHT)
            layouter.minimalNodeDistance = 20.0
            layouter.minimalEdgeDistance = 20.0
            layouter.layerer = GraphManager.getGraphManager().createBFSLayerer()
            return layouter
        }
    }
    
    class Node(
        technology: ParadoxScriptProperty,
        provider: StellarisTechnologyTreeDiagramProvider
    ) : PsiDiagramNode<PsiElement>(technology, provider) {
        override fun getTooltip(): String? {
            val element = identifyingElement
            if(element !is ParadoxScriptProperty) return null
            return StellarisTechnologyHandler.getName(element)
        }
    }
    
    class Edge(
        source: Node,
        target: Node,
        relationship: DiagramRelationshipInfo = REL_PREREQUISITE
    ) : DiagramEdgeBase<PsiElement>(source, target, relationship)
    
    class DataModel(
        project: Project,
        val file: VirtualFile?, //umlFile
        val provider: StellarisTechnologyTreeDiagramProvider
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
            ProgressManager.checkCanceled()
            val settings = provider.extras.additionalDiagramSettings
            val configuration = DiagramConfiguration.getInstance()
            _nodes.clear()
            _edges.clear()
            val originalFile = file?.getUserData(DiagramDataKeys.ORIGINAL_ELEMENT)
            val technologies = StellarisTechnologyHandler.getTechnologies(project, originalFile)
            if(technologies.isEmpty()) return
            val nodeMap = mutableMapOf<ParadoxScriptProperty, Node>()
            val techMap = mutableMapOf<String, ParadoxScriptProperty>()
            for(technology in technologies) {
                ProgressManager.checkCanceled()
                if(!shouldShow(technology, settings, configuration)) continue
                val node = Node(technology, provider)
                nodeMap.put(technology, node)
                techMap.put(StellarisTechnologyHandler.getName(technology), technology)
                _nodes.add(node)
            }
            for(technology in technologies) {
                ProgressManager.checkCanceled()
                val prerequisites = StellarisTechnologyHandler.getPrerequisites(technology)
                if(prerequisites.isEmpty()) continue
                for(prerequisite in prerequisites) {
                    val source = techMap.get(prerequisite)?.let { nodeMap.get(it) } ?: continue
                    val target = nodeMap.get(technology) ?: continue
                    val edge = Edge(source, target)
                    _edges.add(edge)
                }
            }
        }
    
        private fun shouldShow(technology: ParadoxScriptProperty, settings: Array<out DiagramConfigGroup>, configuration: DiagramConfiguration): Boolean {
            for(setting in settings) {
                when(setting.name) {
                    PlsBundle.message("diagram.stellaris.technologyTree.settings.type") -> {
                        for(config in setting.elements) {
                            val enabled = configuration.isEnabledByDefault(provider, config.name)
                            if(enabled) continue
                            when(config.name) {
                                PlsBundle.message("diagram.stellaris.technologyTree.settings.type.start") -> {
                                    val v = technology.findProperty("start_tech", inline = true)
                                        ?.propertyValue?.booleanValue() ?: false
                                    return !v
                                }
                                PlsBundle.message("diagram.stellaris.technologyTree.settings.type.rare") -> {
                                    val v = technology.findProperty("is_rare", inline = true)
                                        ?.propertyValue?.booleanValue() ?: false
                                    return !v
                                }
                                PlsBundle.message("diagram.stellaris.technologyTree.settings.type.dangerous") -> {
                                    val v = technology.findProperty("is_dangerous", inline = true)
                                        ?.propertyValue?.booleanValue() ?: false
                                    return !v
                                }
                                PlsBundle.message("diagram.stellaris.technologyTree.settings.type.repeatable") -> {
                                    val v = technology.findProperty("levels", inline = true)
                                        ?.propertyValue?.intValue()
                                    return v == null
                                }
                            }
                        }
                    }
                    PlsBundle.message("diagram.stellaris.technologyTree.settings.tier") -> {
                        val v = technology.findProperty("tier", inline = true)
                            ?.propertyValue?.stringValue() ?: return false
                        val configName = PlsBundle.message("diagram.stellaris.technologyTree.settings.tier.option", v)
                        for(config in setting.elements) {
                            val enabled = configuration.isEnabledByDefault(provider, config.name)
                            if(enabled) continue
                            return config.name != configName
                        }
                    }
                    PlsBundle.message("diagram.stellaris.technologyTree.settings.area") ->{
                        val v = technology.findProperty("area", inline = true)
                            ?.propertyValue?.stringValue() ?: return false
                        val configName = PlsBundle.message("diagram.stellaris.technologyTree.settings.area.option", v)
                        for(config in setting.elements) {
                            val enabled = configuration.isEnabledByDefault(provider, config.name)
                            if(enabled) continue
                            return config.name != configName
                        }
                    }
                    PlsBundle.message("diagram.stellaris.technologyTree.settings.category") ->{
                        val v = technology.findProperty("category", inline = true)
                            ?.valueList?.mapNotNullTo(mutableSetOf()) { it.stringValue() }.orEmpty()
                        val configNames = v.map { PlsBundle.message("diagram.stellaris.technologyTree.settings.category", it) }
                        for(config in setting.elements) {
                            val enabled = configuration.isEnabledByDefault(provider, config.name)
                            if(enabled) continue
                            return config.name !in configNames
                        }
                    }
                }
            }
            return true
        }
    }
}