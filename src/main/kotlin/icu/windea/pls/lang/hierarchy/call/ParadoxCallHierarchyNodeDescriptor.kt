package icu.windea.pls.lang.hierarchy.call

import com.intellij.codeInsight.highlighting.*
import com.intellij.ide.*
import com.intellij.ide.hierarchy.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.ui.util.*
import com.intellij.openapi.util.*
import com.intellij.pom.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import java.awt.*

//com.intellij.ide.hierarchy.call.CallHierarchyNodeDescriptor

class ParadoxCallHierarchyNodeDescriptor(
    project: Project,
    parentDescriptor: HierarchyNodeDescriptor?,
    element: PsiElement,
    isBase: Boolean,
    val navigateToReference: Boolean
) : HierarchyNodeDescriptor(project, parentDescriptor, element, isBase), Navigatable {
    var usageCount = 1
    val references = mutableListOf<PsiReference>()

    companion object {
        @JvmStatic
        fun getLocationAttributes(): TextAttributes? {
            return UsageTreeColors.NUMBER_OF_USAGES_ATTRIBUTES.toTextAttributes()
        }
    }

    override fun update(): Boolean {
        var changes = super.update()
        val element = psiElement
        if (element == null) {
            return invalidElement()
        }
        if (changes && myIsBase) {
            icon = getBaseMarkerIcon(icon)
        }
        val oldText = myHighlightedText
        myHighlightedText = CompositeAppearance()
        val nameAttributes = if (myColor != null) TextAttributes(myColor, null, null, null, Font.PLAIN) else null
        when (element) {
            is ParadoxScriptScriptedVariable -> {
                val fileInfo = element.fileInfo ?: return invalidElement()
                val name = element.name.orAnonymous()
                myHighlightedText.ending.addText(name, nameAttributes)
                val path = fileInfo.path.path
                val qualifiedName = fileInfo.rootInfo.qualifiedName
                val location = " " + PlsBundle.message("hierarchy.call.descriptor.scriptedVariable.location", path, qualifiedName)
                myHighlightedText.ending.addText(location, getLocationAttributes())
            }
            is ParadoxScriptDefinitionElement -> {
                val definitionInfo = element.definitionInfo ?: return invalidElement()
                val fileInfo = element.fileInfo ?: return invalidElement()
                val name = definitionInfo.name.orAnonymous()
                myHighlightedText.ending.addText(name, nameAttributes)
                val type = definitionInfo.type
                val path = fileInfo.path.path
                val qualifiedName = fileInfo.rootInfo.qualifiedName
                val location = " " + PlsBundle.message("hierarchy.call.descriptor.definition.location", type, path, qualifiedName)
                myHighlightedText.ending.addText(location, getLocationAttributes())
            }
            is ParadoxLocalisationProperty -> {
                val fileInfo = element.fileInfo ?: return invalidElement()
                val name = element.name.orAnonymous()
                myHighlightedText.ending.addText(name, nameAttributes)
                val path = fileInfo.path.path
                val qualifiedName = fileInfo.rootInfo.qualifiedName
                val location = " " + PlsBundle.message("hierarchy.call.descriptor.localisation.location", path, qualifiedName)
                myHighlightedText.ending.addText(location, getLocationAttributes())
            }
        }
        if (usageCount > 1) {
            val text = IdeBundle.message("node.call.hierarchy.N.usages", usageCount)
            myHighlightedText.ending.addText(" $text", getUsageCountPrefixAttributes())
        }
        myName = myHighlightedText.text

        if (!Comparing.equal(myHighlightedText, oldText)) {
            changes = true
        }
        return changes
    }

    override fun navigate(requestFocus: Boolean) {
        if (!navigateToReference) {
            val element = psiElement
            if (element is Navigatable && (element as Navigatable).canNavigate()) {
                (element as Navigatable).navigate(requestFocus)
            }
            return
        }
        val firstReference: PsiReference = references.get(0)
        val element = firstReference.element
        val callElement = element.parent
        if (callElement is Navigatable && (callElement as Navigatable).canNavigate()) {
            (callElement as Navigatable).navigate(requestFocus)
        } else {
            val psiFile = callElement.containingFile
            if (psiFile == null || psiFile.virtualFile == null) return
            FileEditorManager.getInstance(myProject).openFile(psiFile.virtualFile, requestFocus)
        }
        val editor = PsiEditorUtil.findEditor(callElement)
        if (editor != null) {
            val highlightManager = HighlightManager.getInstance(myProject)
            val highlighters = mutableListOf<RangeHighlighter>()
            for (psiReference in references) {
                val eachElement = psiReference.element
                val textRange = eachElement.textRange
                highlightManager.addRangeHighlight(
                    editor, textRange.startOffset, textRange.endOffset,
                    EditorColors.SEARCH_RESULT_ATTRIBUTES, false, highlighters
                )
            }
        }
    }

    override fun canNavigate(): Boolean {
        if (!navigateToReference) {
            val element = psiElement
            return element is Navigatable && element.canNavigate()
        }
        if (references.isEmpty()) return false
        val firstReference: PsiReference = references.get(0)
        val callElement = firstReference.element.parent
        if (callElement == null || !callElement.isValid) return false
        if (callElement !is Navigatable || !(callElement as Navigatable).canNavigate()) {
            val psiFile = callElement.containingFile
            return psiFile != null
        }
        return true
    }

    override fun canNavigateToSource(): Boolean {
        return canNavigate()
    }
}
