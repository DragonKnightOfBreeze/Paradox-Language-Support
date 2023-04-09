package icu.windea.pls.core.hierarchy

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.ui.util.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.psi.*
import java.awt.*

//com.intellij.ide.hierarchy.type.TypeHierarchyNodeDescriptor

class ParadoxDefinitionHierarchyNodeDescriptor(
    project: Project,
    parentDescriptor: HierarchyNodeDescriptor?,
    element: PsiElement,
    isBase: Boolean,
    val name: String,
    val type: Type
) : HierarchyNodeDescriptor(project, parentDescriptor, element, isBase) {
    enum class Type {
        Type, Subtype,NoSubtype, Definition
    }
    
    companion object {
        @JvmStatic
        fun getLocationAttributes(): TextAttributes? {
            return UsageTreeColors.NUMBER_OF_USAGES_ATTRIBUTES.toTextAttributes()
        }
    }
    
    override fun update(): Boolean {
        var changes = super.update()
        val element = psiElement
        if(element == null) {
            return invalidElement()
        }
        if(changes && myIsBase) {
            icon = getBaseMarkerIcon(icon)
        }
        val oldText = myHighlightedText
        myHighlightedText = CompositeAppearance()
        val nameAttributes = if(myColor != null) TextAttributes(myColor, null, null, null, Font.PLAIN) else null
        when(element) {
            is CwtProperty -> {
                if(type == Type.NoSubtype) {
                    val name = PlsBundle.message("hierarchy.definition.descriptor.noSubtype")
                    myHighlightedText.ending.addText(name, getLocationAttributes())
                } else {
                    val typeName = element.name.substringIn("[", "]")
                    myHighlightedText.ending.addText(typeName, nameAttributes)
                    val fileName = element.containingFile?.name
                    if(fileName != null) {
                        val location = " " + PlsBundle.message("hierarchy.definition.descriptor.type.location", fileName)
                        myHighlightedText.ending.addText(location, getLocationAttributes())
                    }
                }
            }
            is ParadoxScriptDefinitionElement -> {
                val name = element.definitionInfo?.name.orAnonymous()
                myHighlightedText.ending.addText(name, nameAttributes)
                val fileInfo = element.fileInfo
                if(fileInfo != null) {
                    val location = " " + PlsBundle.message("hierarchy.definition.descriptor.definition.location", fileInfo.path.path, fileInfo.rootInfo.qualifiedName)
                    myHighlightedText.ending.addText(location, getLocationAttributes())
                }
            }
        }
        myName = myHighlightedText.text
        
        if(!Comparing.equal(myHighlightedText, oldText)) {
            changes = true
        }
        return changes
    }
}