package icu.windea.pls.csv.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement
import icu.windea.pls.lang.psi.ParadoxElementPresentationService
import icu.windea.pls.model.constants.ChronicleStrings
import javax.swing.Icon

object ParadoxCsvElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getIcon(element)?.let { return it }

        return element.icon
    }

    fun getPresentableText(element: PsiElement): String? {
        return when (element) {
            is ParadoxCsvFile -> element.name
            is ParadoxCsvHeader -> ChronicleStrings.headerPlaceholder
            is ParadoxCsvRow -> ChronicleStrings.rowPlaceholder
            is PsiPresentableTextAwareElement -> element.presentableText
            is NavigatablePsiElement -> element.name
            else -> null
        }
    }

    fun getLocationString(element: PsiElement): String? {
        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getLocationString(element)?.let { return it }

        return element.containingFile?.name
    }

    fun getTreeLocationString(element: PsiElement): String? {
        return when (element) {
            is ParadoxCsvColumn -> ParadoxCsvPsiService.getHeaderColumn(element)?.presentableText
            else -> null
        }
    }

    fun getPresentableTextInNavBar(element: PsiElement): String? {
        val p = getPresentableText(element) ?: return null
        val l = getTreeLocationString(element) ?: return p
        return "$p ($l)"
    }

    fun getElementInfoInBreadCrumbs(element: PsiElement): String {
        val p = getPresentableText(element) ?: return ""
        val l = getTreeLocationString(element) ?: return p
        return "$p ($l)"
    }
}
