package icu.windea.pls.script.psi

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

class ParadoxScriptElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        return when (location) {
            UsageViewShortNameLocation.INSTANCE -> ParadoxScriptPsiDescriptionService.getName(element)
            UsageViewLongNameLocation.INSTANCE -> ParadoxScriptPsiDescriptionService.getName(element)
            UsageViewTypeLocation.INSTANCE -> ParadoxScriptPsiDescriptionService.getType(element)
            UsageViewNodeTextLocation.INSTANCE -> ParadoxScriptPsiDescriptionService.getNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> ParadoxScriptPsiDescriptionService.getHighlightUsagesDescription(element)
            else -> null
        }
    }
}
