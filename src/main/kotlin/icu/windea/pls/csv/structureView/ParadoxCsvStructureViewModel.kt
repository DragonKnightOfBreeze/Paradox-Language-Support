package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.lang.util.psi.PlsPsiManager

class ParadoxCsvStructureViewModel(
    editor: Editor?,
    file: PsiFile
) : TextEditorBasedStructureViewModel(editor, file), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
    companion object {
        private val _suitableClasses = arrayOf(ParadoxCsvFile::class.java, ParadoxCsvRow::class.java, ParadoxCsvColumn::class.java)
        private val _sorters = arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun getRoot() = ParadoxCsvFileTreeElement(psiFile as ParadoxCsvFile)

    override fun findAcceptableElement(element: PsiElement?): Any? {
        return PlsPsiManager.findAcceptableElementInStructureView(element, canAttachComments = true) { isSuitable(it) }
    }

    override fun getSuitableClasses() = _suitableClasses

    override fun getSorters() = _sorters

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement) = element is ParadoxCsvFileTreeElement

    override fun isAlwaysLeaf(element: StructureViewTreeElement) = false

    override fun isAutoExpand(element: StructureViewTreeElement) = element is ParadoxCsvFileTreeElement

    override fun isSmartExpand() = false
}
