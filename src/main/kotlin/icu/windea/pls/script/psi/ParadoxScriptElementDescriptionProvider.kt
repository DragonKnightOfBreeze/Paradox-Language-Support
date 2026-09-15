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
            UsageViewShortNameLocation.INSTANCE -> ParadoxScriptElementDescriptionService.getName(element)
            UsageViewLongNameLocation.INSTANCE -> ParadoxScriptElementDescriptionService.getName(element)
            UsageViewTypeLocation.INSTANCE -> ParadoxScriptElementDescriptionService.getType(element)
            UsageViewNodeTextLocation.INSTANCE -> ParadoxScriptElementDescriptionService.getNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> ParadoxScriptElementDescriptionService.getHighlightUsagesDescription(element)
            else -> null
        }
    }
}
