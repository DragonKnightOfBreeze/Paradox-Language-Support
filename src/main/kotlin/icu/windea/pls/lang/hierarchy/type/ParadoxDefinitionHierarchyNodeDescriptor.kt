package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.ui.util.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyNodeType
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.awt.*
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyNodeType as NodeType

//com.intellij.ide.hierarchy.type.TypeHierarchyNodeDescriptor

class ParadoxDefinitionHierarchyNodeDescriptor(
    project: Project,
    parentDescriptor: HierarchyNodeDescriptor?,
    element: PsiElement,
    isBase: Boolean,
    val name: String,
    val type: ParadoxDefinitionHierarchyType,
    val nodeType: ParadoxDefinitionHierarchyNodeType
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
        val hierarchySettings = getSettings().hierarchy
        when (element) {
            is CwtProperty -> {
                if (nodeType == NodeType.NoSubtype) {
                    val name = PlsBundle.message("hierarchy.definition.descriptor.noSubtype")
                    myHighlightedText.ending.addText(name, getLocationAttributes())
                } else {
                    val typeName = element.name.substringIn("[", "]")
                    myHighlightedText.ending.addText(typeName, getNameAttributes(myColor))
                }
            }
            is ParadoxScriptDefinitionElement -> {
                val definitionInfo = element.definitionInfo ?: return invalidElement()
                val name = definitionInfo.name.orAnonymous()
                myHighlightedText.ending.addText(name, getNameAttributes(myColor))
                //it's unnecessary to show type info here
                //val type = definitionInfo.type
                //myHighlightedText.ending.addText(": $type", getTypeAttributes())
            }
        }
        run {
            if (element is CwtProperty) return@run

            if (!(hierarchySettings.showLocalizedName)) return@run
            val localizedName = getLocalizedName(element)
            if (localizedName.isNullOrEmpty()) return@run
            myHighlightedText.ending.addText(" $localizedName", getLocalizedNameAttributes())
        }
        run {
            if (element is CwtProperty) {
                //always show location info here
                val filePath = CwtConfigManager.getFilePath(file) ?: return@run
                myHighlightedText.ending.addText(" in $filePath", getLocationAttributes())
                return@run
            }

            if (!hierarchySettings.showLocationInfo) return@run
            val fileInfo = file.fileInfo ?: return@run
            val text = buildString {
                if (hierarchySettings.showPathInfo) append(" in ").append(fileInfo.path.path)
                if (hierarchySettings.showRootInfo) append(" of ").append(fileInfo.rootInfo.qualifiedName)
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

    companion object {
        @JvmStatic
        private fun getNameAttributes(color: Color?) = if (color == null) null else TextAttributes(color, null, null, null, Font.PLAIN)

        //@JvmStatic
        //private fun getTypeAttributes() = UsageTreeColors.NUMBER_OF_USAGES_ATTRIBUTES.toTextAttributes()

        @JvmStatic
        private fun getLocalizedNameAttributes() = UsageTreeColors.NUMBER_OF_USAGES_ATTRIBUTES.toTextAttributes()

        @JvmStatic
        private fun getLocationAttributes() = UsageTreeColors.NUMBER_OF_USAGES_ATTRIBUTES.toTextAttributes()
    }
}
