package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.presentation.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
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
 * * 可以按作用域过滤要显示的科技。（例如，仅限原版，仅限当前模组）
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
    
    override fun getID() = gameType.title + ".TechnologyTree"
    
    override fun createNodeContentManager() = NodeContentManager()
    
    override fun getElementManager() = _elementManager
    
    abstract override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel) : DataModel
    
    override fun getAllContentCategories() = CATEGORIES
    
    abstract override fun getDiagramSettings(project: Project): ParadoxTechnologyTreeDiagramSettings<*>?
    
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
        
        override fun getEditorTitle(element: PsiElement?, additionalElements: MutableCollection<PsiElement>): String {
            return provider.presentableName
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
                            ParadoxLocalisationTextUIRenderer.render(nodeItem)
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
    
    abstract class DataModel(
        project: Project,
        file: VirtualFile?, //umlFile
        provider: ParadoxDefinitionDiagramProvider
    ) : ParadoxDefinitionDiagramProvider.DataModel(project, file, provider) {
        override fun getModificationTracker() = ParadoxPsiModificationTracker.getInstance(project).ScriptFileTracker("common/technologies:txt")
    }
}