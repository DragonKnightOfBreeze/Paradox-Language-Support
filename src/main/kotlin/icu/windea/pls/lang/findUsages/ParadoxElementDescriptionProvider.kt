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
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxModifierLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        if (location is RefactoringDescriptionLocation) return null
        return when (location) {
            UsageViewShortNameLocation.INSTANCE -> getElementName(element)
            UsageViewLongNameLocation.INSTANCE -> getElementName(element) // NOTE 2.1.4 without name information here
            UsageViewTypeLocation.INSTANCE -> getElementType(element)
            UsageViewNodeTextLocation.INSTANCE -> getElementNodeText(element)
            HighlightUsagesDescriptionLocation.INSTANCE -> getElementHighlightUsagesDescription(element)
            else -> getElementName(element)
        }
    }

    private fun getElementName(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptProperty -> {
                val definitionInfo = element.definitionInfo ?: return null
                definitionInfo.name.or.anonymous()
            }
            is ParadoxLocalisationProperty -> element.type?.let { element.name }
            is ParadoxDynamicValueLightElement -> element.name
            is ParadoxComplexEnumValueLightElement -> element.name
            is ParadoxParameterLightElement -> element.name
            is ParadoxLocalisationParameterLightElement -> element.name
            is ParadoxModifierLightElement -> element.name
            else -> null
        }
    }

    private fun getElementType(element: PsiElement): String? {
        return when (element) {
            is ParadoxScriptProperty -> element.definitionInfo?.let { PlsBundle.message("type.definition") }
            is ParadoxLocalisationProperty -> {
                when (element.type) {
                    ParadoxLocalisationType.Normal -> PlsBundle.message("type.localisation")
                    ParadoxLocalisationType.Synced -> PlsBundle.message("type.syncedLocalisation")
                    null -> null
                }
            }
            is ParadoxDynamicValueLightElement -> {
                val dynamicValueType = element.dynamicValueTypes.firstOrNull() ?: return null
                when (dynamicValueType) {
                    "variable" -> PlsBundle.message("type.variable")
                    else -> PlsBundle.message("type.dynamicValue")
                }
            }
            is ParadoxComplexEnumValueLightElement -> PlsBundle.message("type.complexEnumValue")
            is ParadoxParameterLightElement -> PlsBundle.message("type.parameter")
            is ParadoxLocalisationParameterLightElement -> PlsBundle.message("type.localisationParameter")
            is ParadoxModifierLightElement -> PlsBundle.message("type.modifier")
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
