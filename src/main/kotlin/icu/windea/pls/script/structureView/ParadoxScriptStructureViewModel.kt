package icu.windea.pls.script.structureView

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.util.treeView.smartTree.Grouper
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.psi.PlsPsiManager
import icu.windea.pls.script.navigation.ParadoxScriptNavigationManager
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.structureView.ParadoxScriptStructureFilters.DefinitionsFilter
import icu.windea.pls.script.structureView.ParadoxScriptStructureFilters.PropertiesFilter
import icu.windea.pls.script.structureView.ParadoxScriptStructureFilters.ValuesFilter
import icu.windea.pls.script.structureView.ParadoxScriptStructureFilters.VariablesFilter

class ParadoxScriptStructureViewModel(
    editor: Editor?,
    psiFile: PsiFile
) : TextEditorBasedStructureViewModel(editor, psiFile), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
    companion object {
        private val _groupers = emptyArray<Grouper>()
        private val _sorters = arrayOf(Sorter.ALPHA_SORTER)
        private val _filters = arrayOf(VariablesFilter, DefinitionsFilter, PropertiesFilter, ValuesFilter)
    }

    override fun getRoot() = ParadoxScriptFileTreeElement(psiFile as ParadoxScriptFile)

    override fun findAcceptableElement(element: PsiElement?): Any? {
        return PlsPsiManager.findAcceptableElementInStructureView(element, canAttachComments = true) { isSuitable(it) }
    }

    override fun isSuitable(element: PsiElement?): Boolean {
        return ParadoxScriptNavigationManager.accept(element)
    }

    override fun getGroupers() = _groupers

    override fun getSorters() = _sorters

    override fun getFilters() = _filters

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement) = element is ParadoxScriptFileTreeElement

    override fun isAlwaysLeaf(element: StructureViewTreeElement) = false

    override fun isAutoExpand(element: StructureViewTreeElement) = element is ParadoxScriptFileTreeElement

    override fun isSmartExpand() = false

    // do not expand definitions at top level by default
    @Suppress("UnstableApiUsage")
    override fun getMinimumAutoExpandDepth() = 1
}
