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

class ParadoxElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        return when (location) {
            UsageViewShortNameLocation.INSTANCE -> ParadoxPsiDescriptionService.getName(element)
            UsageViewLongNameLocation.INSTANCE -> ParadoxPsiDescriptionService.getName(element)
            UsageViewTypeLocation.INSTANCE -> ParadoxPsiDescriptionService.getType(element)
            UsageViewNodeTextLocation.INSTANCE -> ParadoxPsiDescriptionService.getNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> ParadoxPsiDescriptionService.getHighlightUsagesDescription(element)
            else -> null
        }
    }
}
