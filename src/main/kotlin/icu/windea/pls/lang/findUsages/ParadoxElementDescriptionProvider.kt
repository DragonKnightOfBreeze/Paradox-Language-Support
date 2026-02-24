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
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.light.ParadoxDefinitionMockElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterElement
import icu.windea.pls.lang.psi.light.ParadoxModifierElement
import icu.windea.pls.lang.psi.light.ParadoxParameterElement
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
            is ParadoxDefinitionMockElement -> element.name
            is ParadoxDynamicValueElement -> element.name
            is ParadoxComplexEnumValueElement -> element.name
            is ParadoxParameterElement -> element.name
            is ParadoxLocalisationParameterElement -> element.name
            is ParadoxModifierElement -> element.name
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
            is ParadoxDefinitionMockElement -> PlsBundle.message("type.definition")
            is ParadoxDynamicValueElement -> PlsBundle.message("type.dynamicValue")
            is ParadoxComplexEnumValueElement -> PlsBundle.message("type.complexEnumValue")
            is ParadoxParameterElement -> PlsBundle.message("type.parameter")
            is ParadoxLocalisationParameterElement -> PlsBundle.message("type.localisationParameter")
            is ParadoxModifierElement -> PlsBundle.message("type.modifier")
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
