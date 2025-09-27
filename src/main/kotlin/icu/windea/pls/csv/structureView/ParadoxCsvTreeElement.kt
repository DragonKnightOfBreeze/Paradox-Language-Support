package icu.windea.pls.csv.structureView

import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import com.intellij.psi.PsiElement
import icu.windea.pls.csv.navigation.ParadoxCsvNavigationManager
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRow
import javax.swing.Icon

abstract class ParadoxCsvTreeElement<T : PsiElement>(element: T) : PsiTreeElementBase<T>(element) {
    override fun getIcon(open: Boolean): Icon? {
        val element = element ?: return null
        return ParadoxCsvNavigationManager.getIcon(element)
    }

    override fun getPresentableText(): String? {
        val element = element ?: return null
        return ParadoxCsvNavigationManager.getPresentableText(element)
    }

    override fun getLocationString(): String? {
        val element = element ?: return null
        return ParadoxCsvNavigationManager.getLocationString(element)
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
