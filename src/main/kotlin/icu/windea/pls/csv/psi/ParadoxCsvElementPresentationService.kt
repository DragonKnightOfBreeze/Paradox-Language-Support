package icu.windea.pls.csv.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.icon
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.lang.psi.ParadoxElementPresentationService
import icu.windea.pls.model.constants.ChronicleStrings
import javax.swing.Icon

object ParadoxCsvElementPresentationService {
    fun getIcon(element: PsiElement): Icon? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getIcon(element)?.let { return it }

        return element.icon
    }

    fun getPresentableText(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return when (element) {
            is ParadoxCsvHeader -> ChronicleStrings.headerPlaceholder
            is ParadoxCsvRow -> ChronicleStrings.rowPlaceholder
            else -> PsiService.getDefaultPresentableText(element)
        }
    }

    fun getLocationString(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        // 3.0.3 promote to semantic level if needed
        ParadoxElementPresentationService.getLocationString(element)?.let { return it }

        return element.containingFile?.name
    }

    fun getTreeLocationString(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        return when (element) {
            is ParadoxCsvColumn -> ParadoxCsvPsiService.getHeaderColumn(element)?.presentableText
            else -> null
        }
    }

    fun getPresentableTextInNavBar(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        val p = getPresentableText(element) ?: return null
        val l = getTreeLocationString(element) ?: return p
        return "$p ($l)"
    }

    fun getElementInfoInBreadCrumbs(element: PsiElement): String? {
        // 3.0.5 precheck (necessary)
        if (!checkLanguage(element)) return null

        val p = getPresentableText(element) ?: return null
        val l = getTreeLocationString(element) ?: return p
        return "$p ($l)"
    }

    private fun checkLanguage(element: PsiElement): Boolean = element.language == ParadoxCsvLanguage
}
