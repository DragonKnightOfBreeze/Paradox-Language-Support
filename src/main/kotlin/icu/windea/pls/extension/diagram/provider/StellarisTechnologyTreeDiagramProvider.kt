package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.DiagramElementManager.*
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
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.data.StellarisTechnologyDataProvider.*
import icu.windea.pls.lang.model.*
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
 * 提供群星的科技树图表。
 * * 可以配置是否显示UI表示（科技卡图片）、科技名、图标、关键属性。
 * * 可以按类型、级别、分类、领域过滤要显示的科技。
 * * TODO 可以按作用域过滤要显示的科技。（例如，仅限原版，仅限当前模组）
 * * 支持任何通用的图表操作。（例如，导出为图片）
 */
@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyTreeDiagramProvider : ParadoxDiagramProvider() {
    val _vfsResolver = ParadoxRootVfsResolver(presentableName)
    val _elementManager = ElementManager()
    val _relationshipManager = RelationshipManager()
    val _colorManager = ColorManager()
    val _extras = Extras()
    
    init {
        _elementManager.setUmlProvider(this)
    }
    
    override fun getID() = "Stellaris.TechnologyTree"
    
    override fun getPresentableName() = PlsBundle.message("diagram.stellaris.technologyTree.name")
    
    override fun getActionName(isPopup: Boolean) = PlsBundle.message("diagram.stellaris.technologyTree.actionName")
    
    override fun createScopeManager(project: Project) = null //TODO
    
    override fun createNodeContentManager() = NodeContentManager()
    
    override fun getVfsResolver() = _vfsResolver
    
    override fun getElementManager() = _elementManager
    
    override fun getRelationshipManager() = _relationshipManager
    
    override fun getColorManager() = _colorManager
    
    override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) = DataModel(project, file, this)
    
    override fun getExtras() = _extras
    
    override fun getAllContentCategories() = CATEGORIES
    
    companion object {
        val CAT_TYPE = DiagramCategory(PlsBundle.lazyMessage("diagram.stellaris.technologyTree.category.type"), PlsIcons.Type, true, false)
        val CAT_PROPERTIES = DiagramCategory(PlsBundle.lazyMessage("diagram.stellaris.technologyTree.category.properties"), PlsIcons.Property, true, false)
        val CAT_NAME = DiagramCategory(PlsBundle.lazyMessage("diagram.stellaris.technologyTree.category.name"), PlsIcons.Localisation, false, false)
        val CAT_ICON = DiagramCategory(PlsBundle.lazyMessage("diagram.stellaris.technologyTree.category.icon"), PlsIcons.Image, false, false)
        val CAT_PRESENTATION = DiagramCategory(PlsBundle.lazyMessage("diagram.stellaris.technologyTree.category.presentation"), PlsIcons.Presentation, false, false)
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
    
    class ElementManager : DiagramElementManagerEx<PsiElement>() {
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
            ProgressManager.checkCanceled()
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
            ProgressManager.checkCanceled()
            return when(nodeElement) {
                is ParadoxScriptProperty -> {
                    val result = mutableListOf<Any>()
                    val typeElement = nodeElement.definitionInfo?.typeConfig?.pointer?.element //should not be null
                    if(typeElement != null) result.add(typeElement)
                    val properties = getProperties(nodeElement)
                    result.addAll(properties)
                    val name = StellarisTechnologyHandler.getLocalizedName(nodeElement)
                    if(name != null) result.add(name)
                    val icon = StellarisTechnologyHandler.getIconFile(nodeElement)
                    if(icon != null) result.add(icon)
                    result.add(nodeElement)
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
                            //科技的名字
                            ParadoxLocalisationTextUIRender.render(nodeItem)
                        }
                        nodeItem is PsiFile -> {
                            //科技的图标
                            val iconUrl = ParadoxDdsUrlResolver.resolveByFile(nodeItem.virtualFile, nodeElement.getUserData(PlsKeys.iconFrame) ?: 0)
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
    
    class ColorManager : DiagramColorManagerBase() {
        override fun getNodeBorderColor(builder: DiagramBuilder, node: DiagramNode<*>?, isSelected: Boolean): Color {
            //基于科技领域和类型
            if(node !is Node) return super.getNodeBorderColor(builder, node, isSelected)
            return doGetNodeBorderColor(node) ?: super.getNodeBorderColor(builder, node, isSelected)
        }
        
        private fun doGetNodeBorderColor(node: Node): Color? {
            //这里使用的颜色是来自灰机wiki的特殊字体颜色
            //https://qunxing.huijiwiki.com/wiki/%E7%A7%91%E6%8A%80
            val data = node.data ?: return null
            return when {
                data.is_dangerous && data.is_rare -> ColorUtil.fromHex("#e8514f")
                data.is_dangerous -> ColorUtil.fromHex("#e8514f")
                data.is_rare -> ColorUtil.fromHex("#9743c4")
                data.area == "physics" -> ColorUtil.fromHex("#2370af")
                data.area == "society" -> ColorUtil.fromHex("#47a05f")
                data.area == "engineering" -> ColorUtil.fromHex("#fbaa29")
                else -> null
            }
        }
    }
    
    class Node(
        technology: ParadoxScriptProperty,
        provider: StellarisTechnologyTreeDiagramProvider,
        val data: Data? = null,
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
        relationship: DiagramRelationshipInfo
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
            doRefreshDataModel()
        }
        
        private fun doRefreshDataModel() {
            ProgressManager.checkCanceled()
            val settings = provider.extras.additionalDiagramSettings
            val configuration = DiagramConfiguration.getInstance()
            _nodes.clear()
            _edges.clear()
            val originalFile = file?.getUserData(DiagramDataKeys.ORIGINAL_ELEMENT)
            val technologies = StellarisTechnologyHandler.getTechnologies(project, originalFile)
            if(technologies.isEmpty()) return
            //群星原版科技有400+
            val nodeMap = mutableMapOf<ParadoxScriptProperty, Node>()
            val techMap = mutableMapOf<String, ParadoxScriptProperty>()
            for(technology in technologies) {
                ProgressManager.checkCanceled()
                val data = technology.getData<Data>()
                if(data != null && !shouldShow(data, settings, configuration)) continue
                val node = Node(technology, provider, data)
                nodeMap.put(technology, node)
                techMap.put(StellarisTechnologyHandler.getName(technology), technology)
                _nodes.add(node)
            }
            for(technology in technologies) {
                ProgressManager.checkCanceled()
                val data = technology.getData<Data>() ?: continue
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
        
        private fun shouldShow(data: Data, settings: Array<out DiagramConfigGroup>, configuration: DiagramConfiguration): Boolean {
            //对于每组配置，只要其中任意一个配置匹配即可
            for(setting in settings) {
                when(setting.name) {
                    PlsBundle.message("diagram.stellaris.technologyTree.settings.type") -> {
                        val start = data.start_tech
                        val rare = data.is_rare
                        val dangerous = data.is_dangerous
                        val repeatable = data.levels != null
                        val other = !start && !rare && !dangerous && !repeatable
                        val enabled = setting.elements.any { config ->
                            val e = configuration.isEnabledByDefault(provider, config.name)
                            when(config.name) {
                                PlsBundle.message("diagram.stellaris.technologyTree.settings.type.start") -> if(start) e else false
                                PlsBundle.message("diagram.stellaris.technologyTree.settings.type.rare") -> if(rare) e else false
                                PlsBundle.message("diagram.stellaris.technologyTree.settings.type.dangerous") -> if(dangerous) e else false
                                PlsBundle.message("diagram.stellaris.technologyTree.settings.type.repeatable") -> if(repeatable) e else false
                                PlsBundle.message("diagram.stellaris.technologyTree.settings.type.other") -> if(other) e else false
                                else -> false
                            }
                        }
                        if(!enabled) return false
                    }
                    PlsBundle.message("diagram.stellaris.technologyTree.settings.tier") -> {
                        val v = data.tier ?: return false
                        val configName = PlsBundle.message("diagram.stellaris.technologyTree.settings.tier.option", v)
                        val enabled = configuration.isEnabledByDefault(provider, configName)
                        if(!enabled) return false
                    }
                    PlsBundle.message("diagram.stellaris.technologyTree.settings.area") -> {
                        val v = data.area ?: return false
                        val configName = PlsBundle.message("diagram.stellaris.technologyTree.settings.area.option", v)
                        val enabled = configuration.isEnabledByDefault(provider, configName)
                        if(!enabled) return false
                    }
                    PlsBundle.message("diagram.stellaris.technologyTree.settings.category") -> {
                        val v = data.category.orEmpty()
                        val configNames = v.map { PlsBundle.message("diagram.stellaris.technologyTree.settings.category.option", it) }
                        val enabled = configNames.any { configName -> configuration.isEnabledByDefault(provider, configName) }
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
                DiagramConfigGroup(PlsBundle.message("diagram.stellaris.technologyTree.settings.type")).apply {
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.type.start"), true))
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.type.rare"), true))
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.type.dangerous"), true))
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.type.repeatable"), true))
                    addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.type.other"), true))
                }.also { add(it) }
                //NOTE tier和category应当是动态获取的
                //NOTE 这里我们无法直接获得project，因此暂且合并所有已打开的项目
                //NOTE 这里的设置名不能包含本地化名字，因为这里的设置名同时也作为设置的ID
                DiagramConfigGroup(PlsBundle.message("diagram.stellaris.technologyTree.settings.tier")).apply {
                    val tiers = ProjectManager.getInstance().openProjects.flatMap { project ->
                        StellarisTechnologyHandler.getTechnologyTiers(project, null)
                    }
                    tiers.forEach {
                        addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.tier.option", it.name), true))
                    }
                }.also { add(it) }
                DiagramConfigGroup(PlsBundle.message("diagram.stellaris.technologyTree.settings.area")).apply {
                    val areas = StellarisTechnologyHandler.getResearchAreas()
                    areas.forEach {
                        addElement(DiagramConfigElement(PlsBundle.message("diagram.stellaris.technologyTree.settings.area.option", it), true))
                    }
                }.also { add(it) }
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
    }
}