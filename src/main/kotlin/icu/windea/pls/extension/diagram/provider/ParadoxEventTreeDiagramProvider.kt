package icu.windea.pls.extension.diagram.provider

import com.intellij.diagram.*
import com.intellij.diagram.presentation.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.extension.diagram.*
import icu.windea.pls.extension.diagram.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.renderers.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 提供事件树图表。
 * * 可以配置是否显示事件标题、图片、关键属性。
 * * 可以按类型过滤要显示的事件。
 * * 可以按作用域过滤要显示的科技。（例如，仅限原版，仅限当前模组）
 * * 支持任何通用的图表操作。（例如，导出为图片）
 */
abstract class ParadoxEventTreeDiagramProvider(gameType: ParadoxGameType) : ParadoxDefinitionDiagramProvider(gameType) {
    companion object {
        val CAT_TYPE = DiagramCategory(PlsDiagramBundle.lazyMessage("eventTree.category.type"), PlsIcons.Nodes.Type, true, false)
        val CAT_PROPERTIES = DiagramCategory(PlsDiagramBundle.lazyMessage("eventTree.category.properties"), PlsIcons.Nodes.Property, true, false)
        val CAT_TITLE = DiagramCategory(PlsDiagramBundle.lazyMessage("eventTree.category.title"), PlsIcons.Nodes.Localisation, false, false)
        val CAT_PICTURE = DiagramCategory(PlsDiagramBundle.lazyMessage("eventTree.category.picture"), PlsIcons.General.Image, false, false)
        val CATEGORIES = arrayOf(CAT_TYPE, CAT_PROPERTIES, CAT_TITLE, CAT_PICTURE)

        val REL_INVOKE = object : DiagramRelationshipInfoAdapter("INVOKE", DiagramLineType.SOLID, PlsDiagramBundle.message("eventTree.rel.invoke")) {
            override fun getTargetArrow() = DELTA
        }
    }

    private val _elementManager by lazy { ElementManager(this) }

    override fun createNodeContentManager() = NodeContentManager()

    override fun getElementManager() = _elementManager

    abstract override fun createDataModel(project: Project, element: PsiElement?, file: VirtualFile?, model: DiagramPresentationModel): DataModel

    override fun getAllContentCategories() = CATEGORIES

    abstract override fun getDiagramSettings(project: Project): ParadoxEventTreeDiagramSettings<*>?

    class NodeContentManager : OrderedDiagramNodeContentManager() {
        override fun isInCategory(nodeElement: Any?, item: Any?, category: DiagramCategory, builder: DiagramBuilder?): Boolean {
            return when {
                item is CwtProperty -> category == CAT_TYPE
                item is ParadoxScriptProperty -> {
                    val definitionInfo = item.definitionInfo
                    if (definitionInfo != null) {
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

    class ElementManager(provider: ParadoxDiagramProvider) : ParadoxDiagramElementManager(provider) {
        override fun isAcceptableAsNode(o: Any?): Boolean {
            return o is PsiDirectory || o is ParadoxScriptProperty
        }

        override fun getEditorTitle(element: PsiElement?, additionalElements: MutableCollection<PsiElement>): String {
            return provider.presentableName
        }

        override fun getElementTitle(element: PsiElement): String? {
            ProgressManager.checkCanceled()
            return when (element) {
                is PsiDirectory -> element.name
                is ParadoxScriptProperty -> ParadoxEventManager.getName(element)
                else -> null
            }
        }

        override fun getNodeTooltip(element: PsiElement?): String? {
            return null
        }

        override fun getNodeItems(nodeElement: PsiElement?, builder: DiagramBuilder): Array<Any> {
            ProgressManager.checkCanceled()
            return when (nodeElement) {
                is ParadoxScriptProperty -> {
                    val result = mutableListOf<Any>()
                    val typeElement = nodeElement.definitionInfo?.typeConfig?.pointer?.element //should not be null
                    if (typeElement != null) result.add(typeElement)
                    val properties = getProperties(nodeElement)
                    result.addAll(properties)
                    val localizedNameElement = ParadoxEventManager.getLocalizedNameElement(nodeElement)
                    if (localizedNameElement != null) result.add(localizedNameElement)
                    val icon = ParadoxEventManager.getIconFile(nodeElement)
                    if (icon != null) result.add(icon)
                    result.toTypedArray()
                }
                else -> emptyArray()
            }
        }

        private fun getProperties(nodeElement: ParadoxScriptProperty): Set<ParadoxScriptProperty> {
            provider as ParadoxEventTreeDiagramProvider
            val itemPropertyKeys = provider.getItemPropertyKeys()
            val properties = sortedSetOf<ParadoxScriptProperty>(compareBy { itemPropertyKeys.indexOf(it.name.lowercase()) })
            nodeElement.block?.processProperty(conditional = true, inline = true) {
                if (it.name.lowercase() in itemPropertyKeys) properties.add(it)
                true
            }
            return properties
        }

        override fun getItemComponent(nodeElement: PsiElement, nodeItem: Any?, builder: DiagramBuilder): JComponent? {
            ProgressManager.checkCanceled()
            return when (nodeElement) {
                is ParadoxScriptProperty -> {
                    when {
                        nodeItem is ParadoxLocalisationProperty -> {
                            //事件标题
                            ParadoxLocalisationTextUIRenderer().render(nodeItem)
                        }
                        nodeItem is PsiFile -> {
                            //事件图片
                            val frameInfo = nodeElement.getUserData(PlsKeys.imageFrameInfo)
                            val iconUrl = ParadoxImageManager.resolveUrlByFile(nodeItem.virtualFile, nodeItem.project, frameInfo)

                            //如果无法解析（包括对应文件不存在的情况）就直接跳过
                            if (!ParadoxImageManager.canResolve(iconUrl)) return null

                            val iconFileUrl = iconUrl.toFileUrl()
                            val icon = iconFileUrl.toIconOrNull()
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
            return when (nodeElement) {
                is ParadoxScriptProperty -> {
                    when {
                        nodeItem is CwtProperty -> PlsIcons.Nodes.Type
                        nodeItem is ParadoxScriptProperty -> {
                            val definitionInfo = nodeItem.definitionInfo
                            if (definitionInfo != null) {
                                null
                            } else {
                                PlsIcons.Nodes.Property
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
                            if (definitionInfo != null) {
                                null
                            } else {
                                val rendered = ParadoxScriptTextRenderer(renderInBlock = true).render(nodeItem)
                                val result = SimpleColoredText(rendered, DEFAULT_TEXT_ATTR)
                                val propertyValue = nodeItem.propertyValue
                                if (propertyValue is ParadoxScriptScriptedVariableReference) {
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
        override fun getModificationTracker(): FilePathBasedModificationTracker {
            return ParadoxModificationTrackers.ScriptFileTracker("events/**/*.txt")
        }
    }
}
