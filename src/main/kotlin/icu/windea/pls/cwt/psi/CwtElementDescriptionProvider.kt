package icu.windea.pls.cwt.psi

import com.intellij.codeInsight.highlighting.HighlightUsagesDescriptionLocation
import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewNodeTextLocation
import com.intellij.usageView.UsageViewShortNameLocation
import com.intellij.usageView.UsageViewTypeLocation

// org.jetbrains.kotlin.idea.base.searching.usages.KotlinElementDescriptionProviderBase
// org.jetbrains.kotlin.idea.findUsages.KotlinElementDescriptionProvider

class CwtElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        return when (location) {
            UsageViewShortNameLocation.INSTANCE -> CwtElementDescriptionService.getName(element)
            UsageViewLongNameLocation.INSTANCE -> CwtElementDescriptionService.getName(element)
            UsageViewTypeLocation.INSTANCE -> CwtElementDescriptionService.getType(element)
            UsageViewNodeTextLocation.INSTANCE -> CwtElementDescriptionService.getNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> CwtElementDescriptionService.getHighlightUsagesDescription(element)
            else -> null
        }
    }
}
