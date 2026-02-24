package icu.windea.pls.lang.findUsages

import com.intellij.codeInsight.highlighting.HighlightUsagesDescriptionLocation
import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.util.RefactoringDescriptionLocation
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewNodeTextLocation
import com.intellij.usageView.UsageViewShortNameLocation
import com.intellij.usageView.UsageViewTypeLocation
import icu.windea.pls.config.CwtConfigType
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtString

class CwtConfigElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        if (location is RefactoringDescriptionLocation) return null
        return when (location) {
            UsageViewShortNameLocation.INSTANCE, UsageViewLongNameLocation.INSTANCE -> getElementName(element)
            UsageViewTypeLocation.INSTANCE -> getElementType(element)
            UsageViewNodeTextLocation.INSTANCE -> getElementNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> getElementHighlightUsagesDescription(element)
            else -> getElementName(element)
        }
    }

    private fun getElementName(element: PsiElement): String? {
        return when (element) {
            is CwtProperty -> getConfigType(element)?.description
            is CwtString -> getConfigType(element)?.description
            else -> null
        }
    }

    private fun getElementType(element: PsiElement): String? {
        return when (element) {
            is CwtProperty -> getConfigType(element)?.let { configType -> CwtConfigManager.getNameByConfigType(element.name, configType) ?: element.name }
            is CwtString -> getConfigType(element)?.let { configType -> CwtConfigManager.getNameByConfigType(element.name, configType) ?: element.name }
            else -> null
        }
    }

    private fun getElementNodeText(element: PsiElement): String? {
        return getElementName(element)?.let { name -> getElementType(element)?.let { type -> "$type $name" } }
    }

    private fun getConfigType(element: PsiElement): CwtConfigType? {
        return CwtConfigManager.getConfigType(element)?.takeIf { it.isReference }
    }

    private fun getElementHighlightUsagesDescription(element: PsiElement): String? {
        return getElementNodeText(element)
    }
}
