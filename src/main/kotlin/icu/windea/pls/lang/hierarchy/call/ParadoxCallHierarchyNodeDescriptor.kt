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
import com.intellij.ui.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
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
            is ParadoxScriptScriptedVariable -> {
                val name = element.name.orAnonymous()
                myHighlightedText.ending.addText(name, getNameAttributes(myColor))
            }
            is ParadoxScriptDefinitionElement -> {
                val definitionInfo = element.definitionInfo ?: return invalidElement()
                val name = definitionInfo.name.orAnonymous()
                myHighlightedText.ending.addText(name, getNameAttributes(myColor))
                val type = definitionInfo.type
                myHighlightedText.ending.addText(": $type", getTypeAttributes())
            }
            is ParadoxLocalisationProperty -> {
                val name = element.name.orAnonymous()
                myHighlightedText.ending.addText(name, getNameAttributes(myColor))
            }
        }
        run {
            if (!(hierarchySettings.showLocalizedName)) return@run
            val localizedName = getLocalizedName(element, file)
            if (localizedName.isNullOrEmpty()) return@run
            myHighlightedText.ending.addText(" $localizedName", getLocalizedNameAttributes())
        }
        run {
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
        run {
            if (usageCount <= 1) return@run
            val text = IdeBundle.message("node.call.hierarchy.N.usages", usageCount)
            myHighlightedText.ending.addText(" $text", getUsageCountPrefixAttributes())
        }
        myName = myHighlightedText.text

        if (!Comparing.equal(myHighlightedText, oldText)) {
            changes = true
        }
        return changes
    }

    private fun getLocalizedName(element: PsiElement, file: PsiFile): String? {
        return when (element) {
            is ParadoxScriptScriptedVariable -> {
                val name = element.name
                if (name.isNullOrEmpty()) return null
                ParadoxScriptedVariableManager.getHintFromExtendedConfig(name, file)
            }
            is ParadoxScriptDefinitionElement -> {
                ParadoxDefinitionManager.getLocalizedNames(element).firstOrNull()
            }
            else -> null
        }
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

    companion object {
        private val grayedAttributes = SimpleTextAttributes.GRAYED_ATTRIBUTES

        @JvmStatic
        private fun getNameAttributes(color: Color?) = if (color == null) null else TextAttributes(color, null, null, null, Font.PLAIN)

        @JvmStatic
        private fun getTypeAttributes() = grayedAttributes

        @JvmStatic
        private fun getLocalizedNameAttributes() = grayedAttributes

        @JvmStatic
        private fun getLocationAttributes() = grayedAttributes
    }
}
