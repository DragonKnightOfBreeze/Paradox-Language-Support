package icu.windea.pls.cwt.structureView

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue
import icu.windea.pls.cwt.psi.isBlockValue
import icu.windea.pls.lang.util.psi.PlsPsiManager

class CwtStructureViewModel(
    editor: Editor?,
    psiFile: PsiFile
) : TextEditorBasedStructureViewModel(editor, psiFile), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
    companion object {
        private val defaultSorters = arrayOf(Sorter.ALPHA_SORTER)
    }

    //指定根节点，一般为psiFile
    override fun getRoot() = CwtFileTreeElement(psiFile as CwtFile)

    //指定在结构视图中的元素
    override fun isSuitable(element: PsiElement?): Boolean {
        return element is CwtFile || element is CwtProperty || (element is CwtValue && element.isBlockValue())
    }

    override fun findAcceptableElement(element: PsiElement?): Any? {
        return PlsPsiManager.findAcceptableElementInStructureView(element, canAttachComments = true) { isSuitable(it) }
    }

    //指定可用的排序器，可自定义
    override fun getSorters() = defaultSorters

    override fun getCurrentEditorElement(): Any? {
        //		val leafElement = super.getLeafElement(dataContext)
        //		if(leafElement is CwtValue && !leafElement.isLonely()) return leafElement.parentOfType<CwtProperty>()
        //		return leafElement
        return super.getCurrentEditorElement()
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        return element is CwtFileTreeElement
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        return false
    }

    override fun isAutoExpand(element: StructureViewTreeElement): Boolean {
        return element is CwtFileTreeElement
    }

    override fun isSmartExpand(): Boolean {
        return false
    }
}

