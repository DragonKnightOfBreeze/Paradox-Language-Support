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
            UsageViewShortNameLocation.INSTANCE -> CwtConfigPsiDescriptionService.getName(element)
            UsageViewLongNameLocation.INSTANCE -> CwtConfigPsiDescriptionService.getName(element)
            UsageViewTypeLocation.INSTANCE -> CwtConfigPsiDescriptionService.getType(element)
            UsageViewNodeTextLocation.INSTANCE -> CwtConfigPsiDescriptionService.getNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> CwtConfigPsiDescriptionService.getHighlightUsagesDescription(element)
            else -> null
        }
    }
}
