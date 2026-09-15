package icu.windea.pls.lang.psi

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

class CwtConfigElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        return when (location) {
            UsageViewShortNameLocation.INSTANCE -> CwtConfigElementDescriptionService.getName(element)
            UsageViewLongNameLocation.INSTANCE -> CwtConfigElementDescriptionService.getName(element)
            UsageViewTypeLocation.INSTANCE -> CwtConfigElementDescriptionService.getType(element)
            UsageViewNodeTextLocation.INSTANCE -> CwtConfigElementDescriptionService.getNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> CwtConfigElementDescriptionService.getHighlightUsagesDescription(element)
            else -> null
        }
    }
}
