package icu.windea.pls.localisation.psi

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

class ParadoxLocalisationElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        return when (location) {
            UsageViewShortNameLocation.INSTANCE -> ParadoxLocalisationPsiDescriptionService.getName(element)
            UsageViewLongNameLocation.INSTANCE -> ParadoxLocalisationPsiDescriptionService.getName(element)
            UsageViewTypeLocation.INSTANCE -> ParadoxLocalisationPsiDescriptionService.getType(element)
            UsageViewNodeTextLocation.INSTANCE -> ParadoxLocalisationPsiDescriptionService.getNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> ParadoxLocalisationPsiDescriptionService.getHighlightUsagesDescription(element)
            else -> null
        }
    }
}
