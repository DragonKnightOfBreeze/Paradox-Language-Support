package icu.windea.pls.lang.findUsages

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.csv.ParadoxCsvLanguage
import icu.windea.pls.lang.psi.ParadoxElementDescriptionService

class ParadoxCsvFindUsagesProvider : FindUsagesProvider {
    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return when (element) {
            is LightElementBase -> element.language === ParadoxCsvLanguage
            else -> false
        }
    }

    override fun getWordsScanner() = ParadoxCsvWordScanner()

    override fun getHelpId(psiElement: PsiElement) = "reference.dialogs.findUsages.other"

    override fun getType(element: PsiElement): String {
        ParadoxElementDescriptionService.getType(element)?.let { return it }
        return ""
    }

    override fun getDescriptiveName(element: PsiElement): String {
        ParadoxElementDescriptionService.getName(element)?.let { return it }
        return ""
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        ParadoxElementDescriptionService.getNodeText(element)?.let { return it }
        return ""
    }
}
