package icu.windea.pls.localisation.structureView

import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.util.psi.PlsPsiManager
import icu.windea.pls.localisation.navigation.ParadoxLocalisationNavigationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile

class ParadoxLocalisationStructureViewModel(
    editor: Editor?,
    file: PsiFile
) : TextEditorBasedStructureViewModel(editor, file), StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
    companion object {
        private val _sorters = arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun getRoot() = ParadoxLocalisationFileTreeElement(psiFile as ParadoxLocalisationFile)

    override fun findAcceptableElement(element: PsiElement?): Any? {
        return PlsPsiManager.findAcceptableElementInStructureView(element, canAttachComments = true) { isSuitable(it) }
    }

    override fun isSuitable(element: PsiElement?): Boolean {
        return ParadoxLocalisationNavigationManager.accept(element)
    }

    override fun getSorters() = _sorters

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement) = isAutoExpandElement(element)

    override fun isAlwaysLeaf(element: StructureViewTreeElement) = false

    override fun isAutoExpand(element: StructureViewTreeElement) = isAutoExpandElement(element)

    override fun isSmartExpand() = false

    private fun isAutoExpandElement(element: StructureViewTreeElement): Boolean {
        return element is ParadoxLocalisationFileTreeElement || element is ParadoxLocalisationPropertyListTreeElement
    }
}
