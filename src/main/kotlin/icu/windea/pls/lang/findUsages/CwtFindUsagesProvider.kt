package icu.windea.pls.lang.findUsages

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtPsiDescriptionService
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.lang.psi.CwtConfigPsiDescriptionService

class CwtFindUsagesProvider : FindUsagesProvider {
    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return when (element) {
            is CwtOption -> true
            is CwtProperty -> true
            is CwtString -> true
            is LightElementBase -> element.language === CwtLanguage
            else -> false
        }
    }

    override fun getWordsScanner(): WordsScanner = CwtWordScanner()

    override fun getHelpId(psiElement: PsiElement) = "reference.dialogs.findUsages.other"

    override fun getType(element: PsiElement): String {
        CwtConfigPsiDescriptionService.getType(element)?.let { return it }
        CwtPsiDescriptionService.getType(element)?.let { return it }
        return ""
    }

    override fun getDescriptiveName(element: PsiElement): String {
        CwtConfigPsiDescriptionService.getName(element)?.let { return it }
        CwtPsiDescriptionService.getName(element)?.let { return it }
        return ""
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        CwtConfigPsiDescriptionService.getNodeText(element)?.let { return it }
        CwtPsiDescriptionService.getNodeText(element)?.let { return it }
        return ""
    }
}
