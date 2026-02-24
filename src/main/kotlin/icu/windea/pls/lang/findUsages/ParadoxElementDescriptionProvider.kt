package icu.windea.pls.lang.findUsages

import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.util.RefactoringDescriptionLocation
import com.intellij.usageView.UsageViewTypeLocation
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.complexEnumValueInfo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterElement
import icu.windea.pls.lang.psi.light.ParadoxModifierElement
import icu.windea.pls.lang.psi.light.ParadoxParameterElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class ParadoxElementDescriptionProvider : ElementDescriptionProvider {
    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        if (location is RefactoringDescriptionLocation) return null
        return when (element) {
            is ParadoxScriptStringExpressionElement -> {
                val complexEnumValueInfo = element.complexEnumValueInfo ?: return null
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("type.complexEnumValue")
                    else -> complexEnumValueInfo.name
                }
            }
            is ParadoxScriptProperty -> {
                val definitionInfo = element.definitionInfo ?: return null
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("type.definition")
                    else -> definitionInfo.name.or.anonymous()
                }
            }
            is ParadoxLocalisationProperty -> {
                val type = element.type ?: return null
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> {
                        when (type) {
                            ParadoxLocalisationType.Normal -> PlsBundle.message("type.localisation")
                            ParadoxLocalisationType.Synced -> PlsBundle.message("type.syncedLocalisation")
                        }
                    }
                    else -> element.name
                }
            }
            is ParadoxDynamicValueElement -> {
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("type.dynamicValue")
                    else -> element.name
                }
            }
            is ParadoxParameterElement -> {
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("type.parameter")
                    else -> element.name
                }
            }
            is ParadoxLocalisationParameterElement -> {
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("type.localisationParameter")
                    else -> element.name
                }
            }
            is ParadoxModifierElement -> {
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("type.modifier")
                    else -> element.name
                }
            }
            else -> null
        }
    }
}
