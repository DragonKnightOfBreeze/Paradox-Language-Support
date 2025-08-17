package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.*
import com.intellij.ide.util.treeView.smartTree.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.util.*

class ParadoxCsvStructureViewModel(
    editor: Editor?,
    file: PsiFile
) : TextEditorBasedStructureViewModel(editor, file), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
    companion object {
        private val defaultSorters = arrayOf(Sorter.ALPHA_SORTER)
    }

    //指定根节点，一般为psiFile
    override fun getRoot() = ParadoxCsvFileTreeElement(psiFile as ParadoxCsvFile)

    //指定在结构视图中的元素
    override fun isSuitable(element: PsiElement?): Boolean {
        return element is ParadoxCsvFile || element is ParadoxCsvRow || element is ParadoxCsvColumn
    }

    override fun findAcceptableElement(element: PsiElement?): Any? {
        return PlsPsiManager.findAcceptableElementInStructureView(element, canAttachComment = true) { isSuitable(it) }
    }

    //指定可用的排序器，可自定义
    override fun getSorters() = defaultSorters

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        return element is ParadoxCsvFileTreeElement
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        return false
    }

    override fun isAutoExpand(element: StructureViewTreeElement): Boolean {
        return element is ParadoxCsvFileTreeElement
    }

    override fun isSmartExpand(): Boolean {
        return false
    }
}
