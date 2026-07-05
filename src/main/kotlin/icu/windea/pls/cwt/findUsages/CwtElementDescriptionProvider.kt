package icu.windea.pls.cwt.findUsages

import com.intellij.codeInsight.highlighting.HighlightUsagesDescriptionLocation
import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.util.RefactoringDescriptionLocation
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewNodeTextLocation
import com.intellij.usageView.UsageViewShortNameLocation
import com.intellij.usageView.UsageViewTypeLocation
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtString

class CwtElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        if (location is RefactoringDescriptionLocation) return null
        return when (location) {
            UsageViewShortNameLocation.INSTANCE -> getElementName(element)
            UsageViewLongNameLocation.INSTANCE -> getElementName(element)
            UsageViewTypeLocation.INSTANCE -> getElementType(element)
            UsageViewNodeTextLocation.INSTANCE -> getElementNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> getElementHighlightUsagesDescription(element)
            else -> getElementName(element)
        }
    }

    private fun getElementName(element: PsiElement): String? {
        return when (element) {
            is CwtOption -> element.name
            is CwtProperty -> element.name
            is CwtString -> element.name
            else -> null
        }
    }

    private fun getElementType(element: PsiElement): String? {
        return when (element) {
            is CwtOption -> ChronicleBundle.message("cwt.type.option")
            is CwtProperty -> ChronicleBundle.message("cwt.type.property")
            is CwtString -> ChronicleBundle.message("cwt.type.value")
            else -> null
        }
    }

    private fun getElementNodeText(element: PsiElement): String? {
        return getElementName(element)?.let { name -> getElementType(element)?.let { type -> "$type $name" } ?: name }
    }

    private fun getElementHighlightUsagesDescription(element: PsiElement): String? {
        return getElementNodeText(element)
    }
}
