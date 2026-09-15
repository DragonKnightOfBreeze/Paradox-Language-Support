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
            UsageViewShortNameLocation.INSTANCE -> ParadoxLocalisationElementDescriptionService.getName(element)
            UsageViewLongNameLocation.INSTANCE -> ParadoxLocalisationElementDescriptionService.getName(element)
            UsageViewTypeLocation.INSTANCE -> ParadoxLocalisationElementDescriptionService.getType(element)
            UsageViewNodeTextLocation.INSTANCE -> ParadoxLocalisationElementDescriptionService.getNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> ParadoxLocalisationElementDescriptionService.getHighlightUsagesDescription(element)
            else -> null
        }
    }
}
