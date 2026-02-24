package icu.windea.pls.cwt.findUsages

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.ElementDescriptionUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewNodeTextLocation
import com.intellij.usageView.UsageViewTypeLocation
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.editor.CwtWordScanner
import icu.windea.pls.cwt.psi.CwtBlock
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtString

class CwtFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner = CwtWordScanner()

    override fun canFindUsagesFor(element: PsiElement): Boolean {
        val element = if (element.elementType == CwtElementTypes.LEFT_BRACE) element.parent else element
        return when (element) {
            is CwtProperty -> true
            is CwtString -> true
            is CwtBlock -> true
            is LightElementBase -> element.language == CwtLanguage
            else -> false
        }
    }

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
