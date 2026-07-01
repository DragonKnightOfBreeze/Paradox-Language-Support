package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvPsiPresentationService
import icu.windea.pls.csv.psi.ParadoxCsvRow
import javax.swing.Icon

abstract class ParadoxCsvTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    override fun getPresentableText(): String? {
        val element = element ?: return null
        return ParadoxCsvPsiPresentationService.getTreePresentableText(element)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        return ParadoxCsvPsiPresentationService.getTreeLocationString(element)
    }

    override fun getIcon(open: Boolean): Icon? {
        val element = element ?: return null
        return ParadoxCsvPsiPresentationService.getIcon(element)
    }

    protected fun PsiElement.toTreeElement(): ParadoxCsvTreeElement<out PsiElement>? {
        return when (this) {
            is ParadoxCsvHeader -> ParadoxCsvHeaderTreeElement(this)
            is ParadoxCsvRow -> ParadoxCsvRowTreeElement(this)
            is ParadoxCsvColumn -> ParadoxCsvColumnTreeElement(this)
            else -> null
        }
    }
}
