package icu.windea.pls.lang.hierarchy.call

import com.intellij.codeInsight.highlighting.HighlightManager
import com.intellij.ide.IdeBundle
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor
import com.intellij.openapi.editor.colors.EditorColors
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.util.CompositeAppearance
import com.intellij.openapi.util.Comparing
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiEditorUtil
import com.intellij.ui.SimpleTextAttributes
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.ParadoxDefinitionManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.qualifiedName
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import java.awt.Color
import java.awt.Font

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
        val hierarchySettings = PlsFacade.getSettings().hierarchy
        when (element) {
            is ParadoxScriptScriptedVariable -> {
                val name = element.name.or.anonymous()
                myHighlightedText.ending.addText(name, getNameAttributes(myColor))
            }
            is ParadoxScriptDefinitionElement -> {
                val definitionInfo = element.definitionInfo ?: return invalidElement()
                val name = definitionInfo.name.or.anonymous()
                myHighlightedText.ending.addText(name, getNameAttributes(myColor))
                val type = definitionInfo.type
                myHighlightedText.ending.addText(": $type", getTypeAttributes())
            }
            is ParadoxLocalisationProperty -> {
                val name = element.name.or.anonymous()
                myHighlightedText.ending.addText(name, getNameAttributes(myColor))
            }
        }
        run {
            if (!(hierarchySettings.showLocalizedName)) return@run
            val localizedName = getLocalizedName(element)
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

    private fun getLocalizedName(element: PsiElement): String? {
        // ParadoxHintTextProvider.getHintText(element)?.let { return it }
        return when (element) {
            is ParadoxScriptScriptedVariable -> ParadoxScriptedVariableManager.getLocalizedName(element)
            is ParadoxScriptDefinitionElement -> ParadoxDefinitionManager.getLocalizedNames(element).firstOrNull()
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
