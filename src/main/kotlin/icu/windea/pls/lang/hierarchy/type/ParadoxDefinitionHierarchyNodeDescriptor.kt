package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.util.CompositeAppearance
import com.intellij.openapi.util.Comparing
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.SimpleTextAttributes
import icu.windea.pls.ChronicleDocBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.base.settings.ChronicleSettings
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.lang.util.ParadoxTechnologyManager
import icu.windea.pls.model.ParadoxGameType
import java.awt.Color
import java.awt.Font
import javax.swing.Icon
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyNodeType as NodeType
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyType as Type

// com.intellij.ide.hierarchy.type.TypeHierarchyNodeDescriptor

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
        val hierarchySettings = ChronicleSettings.getInstance().state.hierarchy
        val name = name.orAnonymous()
        myHighlightedText.ending.addText(name, getNameAttributes(myColor))
        run {
            if (nodeType.grouped) {
                val groupName = getGroupName(file, name)
                if (groupName.isNullOrEmpty()) return@run
                myHighlightedText.ending.addText(" $groupName", getPresentableNameAttributes())
                return@run
            }

            if (nodeType != NodeType.Definition || element !is ParadoxDefinitionElement) return@run
            if (!(hierarchySettings.showPresentableName)) return@run
            val presentableName = getPresentableName(element)
            if (presentableName.isNullOrEmpty()) return@run
            myHighlightedText.ending.addText(" $presentableName", getPresentableNameAttributes())
        }
        run {
            if (type != Type.EventTreeInvoker && type != Type.EventTreeInvoked) return@run
            if (nodeType != NodeType.Definition || element !is ParadoxDefinitionElement) return@run
            if (!hierarchySettings.showEventInfo) return@run
            val eventInfo = getEventInfo(element, hierarchySettings)
            if (eventInfo.isNullOrEmpty()) return@run
            myHighlightedText.ending.addText(eventInfo, getRelatedInfoAttributes())
        }
        run {
            if (type != Type.TechTreePre && type != Type.TechTreePost) return@run
            if (nodeType != NodeType.Definition || element !is ParadoxDefinitionElement) return@run
            if (!hierarchySettings.showTechInfo) return@run
            val techInfo = getTechInfo(element, hierarchySettings)
            if (techInfo.isNullOrEmpty()) return@run
            myHighlightedText.ending.addText(techInfo, getRelatedInfoAttributes())
        }
        run {
            if (nodeType == NodeType.Type || nodeType == NodeType.Subtype) {
                // always show location info here
                val filePath = CwtConfigManager.getFilePath(file) ?: return@run
                myHighlightedText.ending.addText(" in $filePath", getLocationAttributes())
                return@run
            }

            if (nodeType != NodeType.Definition || element !is ParadoxDefinitionElement) return@run
            if (!hierarchySettings.showLocationInfo) return@run
            val locationInfo = getLocationInfo(file, hierarchySettings)
            if (locationInfo.isNullOrEmpty()) return@run
            myHighlightedText.ending.addText(locationInfo, getLocationAttributes())
        }
        myName = myHighlightedText.text

        if (!Comparing.equal(myHighlightedText, oldText)) {
            changes = true
        }
        return changes
    }

    private fun getGroupName(file: PsiFile?, name: String): String? {
        val gameType = selectGameType(file)
        val presentableName = when (nodeType) {
            NodeType.EventType -> ChronicleDocBundle.eventType(name, gameType)
            NodeType.TechTier -> ChronicleDocBundle.technologyTier(name, gameType)
            NodeType.TechArea -> ChronicleDocBundle.technologyArea(name, gameType, project, file)
            NodeType.TechCategory -> ChronicleDocBundle.technologyCategory(name, gameType, project, file)
            else -> null
        }
        return presentableName
    }

    private fun getPresentableName(element: PsiElement): String? {
        ParadoxPsiPresentationService.getPresentableNameForDefinition(element)?.let { return it }
        return null
    }

    private fun getEventInfo(element: PsiElement, hierarchySettings: ChronicleSettings.HierarchyState): String? {
        if (element !is ParadoxDefinitionElement) return null
        val definitionInfo = element.definitionInfo ?: return null
        val gameType = definitionInfo.gameType
        return buildList {
            run r@{
                if (!hierarchySettings.showEventInfoByType) return@r
                val s = ParadoxEventManager.getType(definitionInfo)
                    ?.orNull()?.let { ChronicleDocBundle.eventType(it, gameType) }
                this += s ?: "-"
            }
            run r@{
                if (!hierarchySettings.showEventInfoByAttributes) return@r
                val s = ParadoxEventManager.getAttributes(definitionInfo)
                    .joinToString(", ") { ChronicleDocBundle.eventAttribute(it, gameType) }.orNull()
                this += s
            }
        }.filterNotNull().joinToString(" / ", " [", "]")
    }

    private fun getTechInfo(element: PsiElement, hierarchySettings: ChronicleSettings.HierarchyState): String? {
        if (element !is ParadoxDefinitionElement) return null
        val definitionInfo = element.definitionInfo ?: return null
        val gameType = definitionInfo.gameType
        if (gameType != ParadoxGameType.Stellaris) return null // TODO 3.0.x refactor
        val file = element.containingFile
        return buildList {
            run r@{
                if (!hierarchySettings.showTechInfoByTier) return@r
                val s = ParadoxTechnologyManager.Stellaris.getTier(element)
                    ?.orNull()?.let { ChronicleDocBundle.technologyTier(it, gameType) }
                this += s ?: "-"
            }
            run r@{
                if (!hierarchySettings.showTechInfoByArea) return@r
                val s = ParadoxTechnologyManager.Stellaris.getArea(element)
                    ?.orNull()?.let { ChronicleDocBundle.technologyArea(it, gameType, project, file) }
                this += s ?: "-"
            }
            run r@{
                if (!hierarchySettings.showTechInfoByCategories) return@r
                val s = ParadoxTechnologyManager.Stellaris.getCategories(element)
                    .joinToString(", ") { ChronicleDocBundle.technologyCategory(it, gameType, project, file) }.orNull()
                this += s ?: "-"
            }
            run r@{
                if (!hierarchySettings.showTechInfoByAttributes) return@r
                val s = ParadoxTechnologyManager.Stellaris.getAttributes(definitionInfo)
                    .joinToString(", ") { ChronicleDocBundle.technologyAttribute(it, gameType) }.orNull()
                this += s
            }
        }.filterNotNull().joinToString(" / ", " [", "]")
    }

    private fun getLocationInfo(file: PsiFile?, hierarchySettings: ChronicleSettings.HierarchyState): String? {
        val fileInfo = file?.fileInfo ?: return null
        return buildString {
            if (hierarchySettings.showLocationInfoByPath) {
                append(" in ").append(fileInfo.path.path)
            }
            if (hierarchySettings.showLocationInfoByRootInfo) {
                append(" of ").append(fileInfo.rootInfo.qualifiedName)
            }
        }
    }

    override fun getIcon(element: PsiElement): Icon? {
        if (nodeType.grouped) return ChronicleIcons.Nodes.DefinitionGroup
        return super.getIcon(element)
    }

    companion object {
        private val grayedAttributes = SimpleTextAttributes.GRAYED_ATTRIBUTES

        @JvmStatic
        private fun getNameAttributes(color: Color?) = if (color == null) null else TextAttributes(color, null, null, null, Font.PLAIN)

        @JvmStatic
        private fun getPresentableNameAttributes() = grayedAttributes

        @JvmStatic
        private fun getRelatedInfoAttributes() = grayedAttributes

        @JvmStatic
        private fun getLocationAttributes() = grayedAttributes
    }
}
