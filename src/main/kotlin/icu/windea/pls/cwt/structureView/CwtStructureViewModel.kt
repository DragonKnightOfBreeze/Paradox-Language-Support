package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.cwt.navigation.CwtNavigationManager
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.lang.util.PlsPsiManager

class CwtStructureViewModel(
    editor: Editor?,
    psiFile: PsiFile
) : TextEditorBasedStructureViewModel(editor, psiFile), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
    companion object {
        private val _sorters = arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun getRoot() = CwtFileTreeElement(psiFile as CwtFile)

    override fun findAcceptableElement(element: PsiElement?): Any? {
        return PlsPsiManager.findAcceptableElementInStructureView(element, canAttachComments = true) { isSuitable(it) }
    }

    override fun isSuitable(element: PsiElement?): Boolean {
        return CwtNavigationManager.accept(element)
    }

    override fun getSorters() = _sorters

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement) = element is CwtFileTreeElement

    override fun isAlwaysLeaf(element: StructureViewTreeElement) = false

    override fun isAutoExpand(element: StructureViewTreeElement) = element is CwtFileTreeElement

    override fun isSmartExpand() = false
}
