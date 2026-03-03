package icu.windea.pls.csv.findUsages

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.ElementDescriptionUtil
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewNodeTextLocation
import com.intellij.usageView.UsageViewTypeLocation
import icu.windea.pls.csv.editor.ParadoxCsvWordScanner

class ParadoxCsvFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner() = ParadoxCsvWordScanner()

    override fun canFindUsagesFor(element: PsiElement) = false

    override fun getHelpId(psiElement: PsiElement) = "reference.dialogs.findUsages.other"

    override fun getType(element: PsiElement): String {
        return ElementDescriptionUtil.getElementDescription(element, UsageViewTypeLocation.INSTANCE)
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return ElementDescriptionUtil.getElementDescription(element, UsageViewLongNameLocation.INSTANCE)
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return ElementDescriptionUtil.getElementDescription(element, UsageViewNodeTextLocation.INSTANCE)
    }
}
