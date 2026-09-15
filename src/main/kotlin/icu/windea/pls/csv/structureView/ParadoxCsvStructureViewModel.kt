package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.csv.psi.ParadoxCsvFile

class ParadoxCsvStructureViewModel(
    editor: Editor?,
    file: PsiFile
) : TextEditorBasedStructureViewModel(editor, file), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
    companion object {
        private val _sorters = arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun findAcceptableElement(element: PsiElement?): Any? {
        return PsiService.findAcceptableElementInStructureView(element, canAttachComments = true) { isSuitable(it) }
    }

    override fun isSuitable(element: PsiElement?): Boolean {
        return when (element) {
            is ParadoxCsvFile -> true
            is ParadoxCsvColumnContainer -> true
            is ParadoxCsvColumn -> true
            else -> false
        }
    }

    override fun getRoot() = ParadoxCsvFileTreeElement(psiFile as ParadoxCsvFile)

    override fun getSorters() = _sorters

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement) = element is ParadoxCsvFileTreeElement

    override fun isAlwaysLeaf(element: StructureViewTreeElement) = false

    override fun isAutoExpand(element: StructureViewTreeElement) = element is ParadoxCsvFileTreeElement

    override fun isSmartExpand() = false
}
