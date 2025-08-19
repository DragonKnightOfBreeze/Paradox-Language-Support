package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.ui.util.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.awt.*
import javax.swing.*
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyNodeType as NodeType
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyType as Type

//com.intellij.ide.hierarchy.type.TypeHierarchyNodeDescriptor

class ParadoxDefinitionHierarchyNodeDescriptor(
    project: Project,
    parentDescriptor: HierarchyNodeDescriptor?,
    element: PsiElement,
    isBase: Boolean,
    val name: String,
    val type: Type,
    val nodeType: NodeType
) : HierarchyNodeDescriptor(project, parentDescriptor, element, isBase) {
    override fun update(): Boolean {
        var changes = super.update()
        val element = psiElement
        if (element == null) return invalidElement()
        if (changes && myIsBase) {
            icon = getBaseMarkerIcon(icon)
        }
        val oldText = myHighlightedText
        myHighlightedText = CompositeAppearance()
        val file = element.containingFile
        val hierarchySettings = PlsFacade.getSettings().hierarchy
        val name = name.or.anonymous()
        myHighlightedText.ending.addText(name, getNameAttributes(myColor))
        run {
            if (nodeType.grouped) {
                val gameType = selectGameType(file)
                val localizedName = when (nodeType) {
                    NodeType.EventType -> PlsDocBundle.eventType(name, gameType)
                    NodeType.TechTier -> PlsDocBundle.technologyTier(name, gameType)
                    NodeType.TechArea -> PlsDocBundle.technologyArea(name, gameType, project, file)
                    NodeType.TechCategory -> PlsDocBundle.technologyCategory(name, gameType, project, file)
                    else -> return@run //unexpected
                }
                if (localizedName.isEmpty()) return@run
                myHighlightedText.ending.addText(" $localizedName", getLocalizedNameAttributes())
                return@run
            }

            if (nodeType != NodeType.Definition || element !is ParadoxScriptDefinitionElement) return@run
            if (!(hierarchySettings.showLocalizedName)) return@run
            val localizedName = getLocalizedName(element)
            if (localizedName.isNullOrEmpty()) return@run
            myHighlightedText.ending.addText(" $localizedName", getLocalizedNameAttributes())
        }
        run {
            if (type != Type.EventTreeInvoker && type != Type.EventTreeInvoked) return@run
            if (nodeType != NodeType.Definition || element !is ParadoxScriptDefinitionElement) return@run
            if (!hierarchySettings.showEventInfo) return@run
            val definitionInfo = element.definitionInfo ?: return@run
            val gameType = definitionInfo.gameType
            val infos = buildList {
                run r@{
                    if (!hierarchySettings.showEventInfoByType) return@r
                    val s = ParadoxEventManager.getType(definitionInfo)
                        ?.orNull()?.let { PlsDocBundle.eventType(it, gameType) }
                    this += s ?: "-"
                }
                run r@{
                    if (!hierarchySettings.showEventInfoByAttributes) return@r
                    val s = ParadoxEventManager.getAttributes(definitionInfo)
                        .joinToString(", ") { PlsDocBundle.eventAttribute(it, gameType) }.orNull()
                    this += s
                }
            }.filterNotNull()
            myHighlightedText.ending.addText(joinInfos(infos), getRelatedInfoAttributes())
        }
        run {
            if (type != Type.TechTreePre && type != Type.TechTreePost) return@run
            if (nodeType != NodeType.Definition || element !is ParadoxScriptDefinitionElement) return@run
            if (!hierarchySettings.showTechInfo) return@run
            val definitionInfo = element.definitionInfo ?: return@run
            if (definitionInfo.gameType != ParadoxGameType.Stellaris) return@run
            val gameType = definitionInfo.gameType
            val infos = buildList {
                run r@{
                    if (!hierarchySettings.showTechInfoByTier) return@r
                    val s = ParadoxTechnologyManager.Stellaris.getTier(element)
                        ?.orNull()?.let { PlsDocBundle.technologyTier(it, gameType) }
                    this += s ?: "-"

                }
                run r@{
                    if (!hierarchySettings.showTechInfoByArea) return@r
                    val s = ParadoxTechnologyManager.Stellaris.getArea(element)
                        ?.orNull()?.let { PlsDocBundle.technologyArea(it, gameType, project, file) }
                    this += s ?: "-"
                }
                run r@{
                    if (!hierarchySettings.showTechInfoByCategories) return@r
                    val s = ParadoxTechnologyManager.Stellaris.getCategories(element)
                        .joinToString(", ") { PlsDocBundle.technologyCategory(it, gameType, project, file) }.orNull()
                    this += s ?: "-"
                }
                run r@{
                    if (!hierarchySettings.showTechInfoByAttributes) return@r
                    val s = ParadoxTechnologyManager.Stellaris.getAttributes(definitionInfo)
                        .joinToString(", ") { PlsDocBundle.technologyAttribute(it, gameType) }.orNull()
                    this += s
                }
            }.filterNotNull()
            myHighlightedText.ending.addText(joinInfos(infos), getRelatedInfoAttributes())
        }
        run {
            if (nodeType == NodeType.Type || nodeType == NodeType.Subtype) {
                //always show location info here
                val filePath = CwtConfigManager.getFilePath(file) ?: return@run
                myHighlightedText.ending.addText(" in $filePath", getLocationAttributes())
                return@run
            }

            if (nodeType != NodeType.Definition || element !is ParadoxScriptDefinitionElement) return@run
            if (!hierarchySettings.showLocationInfo) return@run
            val fileInfo = file.fileInfo ?: return@run
            val text = buildString {
                if (hierarchySettings.showLocationInfoByPath) {
                    append(" in ").append(fileInfo.path.path)
                }
                if (hierarchySettings.showLocationInfoByRootInfo) {
                    append(" of ").append(fileInfo.rootInfo.qualifiedName)
                }
            }
            if (text.isEmpty()) return@run
            myHighlightedText.ending.addText(text, getLocationAttributes())
        }
        myName = myHighlightedText.text

        if (!Comparing.equal(myHighlightedText, oldText)) {
            changes = true
        }
        return changes
    }

    private fun getLocalizedName(element: PsiElement): String? {
        if (element !is ParadoxScriptDefinitionElement) return null
        return ParadoxDefinitionManager.getLocalizedNames(element).firstOrNull()
    }

    override fun getIcon(element: PsiElement): Icon? {
        if (nodeType.grouped) return PlsIcons.Nodes.DefinitionGroup
        return super.getIcon(element)
    }

    companion object {
        private val grayedAttributes = SimpleTextAttributes.GRAYED_ATTRIBUTES

        @JvmStatic
        private fun getNameAttributes(color: Color?) = if (color == null) null else TextAttributes(color, null, null, null, Font.PLAIN)

        @JvmStatic
        private fun getLocalizedNameAttributes() = grayedAttributes

        @JvmStatic
        private fun getRelatedInfoAttributes() = grayedAttributes

        @JvmStatic
        private fun getLocationAttributes() = grayedAttributes

        @JvmStatic
        private fun joinInfos(infos: Collection<String>) = infos.joinToString(" / ", " [", "]")
    }
}
