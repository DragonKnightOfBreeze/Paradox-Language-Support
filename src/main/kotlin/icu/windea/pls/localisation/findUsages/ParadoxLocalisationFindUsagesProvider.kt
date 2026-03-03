package icu.windea.pls.localisation.findUsages

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.ElementDescriptionUtil
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewNodeTextLocation
import com.intellij.usageView.UsageViewTypeLocation
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.editor.ParadoxLocalisationWordScanner
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

class ParadoxLocalisationFindUsagesProvider : FindUsagesProvider {
    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return when (element) {
            is ParadoxLocalisationProperty -> true
            is LightElementBase -> element.language == ParadoxLocalisationLanguage
            else -> false
        }
    }

    override fun getWordsScanner() = ParadoxLocalisationWordScanner()

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
