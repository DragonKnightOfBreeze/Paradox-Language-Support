package icu.windea.pls.core.hierarchy.call

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.ui.util.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*
import java.awt.*

//com.intellij.ide.hierarchy.call.CallHierarchyNodeDescriptor

class ParadoxCallHierarchyNodeDescriptor(
    project: Project,
    parentDescriptor: HierarchyNodeDescriptor?,
    element: PsiElement,
    isBase: Boolean,
) : HierarchyNodeDescriptor(project, parentDescriptor, element, isBase) {
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
            is ParadoxScriptScriptedVariable -> {
                val fileInfo = element.fileInfo  ?: return invalidElement()
                val name = element.name.orAnonymous()
                myHighlightedText.ending.addText(name, nameAttributes)
                val path = fileInfo.path.path
                val qualifiedName = fileInfo.rootInfo.qualifiedName
                val location = " " + PlsBundle.message("hierarchy.call.descriptor.scriptedVariable.location", path, qualifiedName)
                myHighlightedText.ending.addText(location, getLocationAttributes())
            }
            is ParadoxScriptDefinitionElement -> {
                val definitionInfo = element.definitionInfo ?: return invalidElement()
                val fileInfo = element.fileInfo  ?: return invalidElement()
                val name = definitionInfo.name.orAnonymous()
                myHighlightedText.ending.addText(name, nameAttributes)
                val type = definitionInfo.type
                val path = fileInfo.path.path
                val qualifiedName = fileInfo.rootInfo.qualifiedName
                val location = " " + PlsBundle.message("hierarchy.call.descriptor.definition.location", type, path, qualifiedName)
                myHighlightedText.ending.addText(location, getLocationAttributes())
            }
        }
        myName = myHighlightedText.text
        
        if(!Comparing.equal(myHighlightedText, oldText)) {
            changes = true
        }
        return changes
    }
}