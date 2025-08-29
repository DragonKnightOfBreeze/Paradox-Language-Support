package icu.windea.pls.localisation.editor

import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.ElementDescriptionLocation
import com.intellij.psi.ElementDescriptionProvider
import com.intellij.psi.PsiElement
import com.intellij.refactoring.util.RefactoringDescriptionLocation
import com.intellij.usageView.UsageViewLongNameLocation
import com.intellij.usageView.UsageViewNodeTextLocation
import com.intellij.usageView.UsageViewTypeLocation
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.localisationInfo
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxLocalisationParameterElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType

class ParadoxLocalisationFindUsagesProvider : FindUsagesProvider, ElementDescriptionProvider {
    override fun getType(element: PsiElement): String {
        return getElementDescription(element, UsageViewTypeLocation.INSTANCE).orEmpty()
    }

    override fun getDescriptiveName(element: PsiElement): String {
        return getElementDescription(element, UsageViewLongNameLocation.INSTANCE).orEmpty()
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        return getElementDescription(element, UsageViewNodeTextLocation.INSTANCE).orEmpty()
    }

    override fun getElementDescription(element: PsiElement, location: ElementDescriptionLocation): String? {
        if (element is RefactoringDescriptionLocation) return null
        return when (element) {
            is ParadoxLocalisationProperty -> {
                val localisationInfo = element.localisationInfo
                if (localisationInfo != null) {
                    when (location) {
                        UsageViewTypeLocation.INSTANCE -> {
                            when (localisationInfo.type) {
                                ParadoxLocalisationType.Normal -> PlsBundle.message("localisation.description.localisation")
                                ParadoxLocalisationType.Synced -> PlsBundle.message("localisation.description.syncedLocalisation")
                            }
                        }
                        else -> element.name
                    }
                } else {
                    when (location) {
                        UsageViewTypeLocation.INSTANCE -> PlsBundle.message("localisation.description.property")
                        else -> element.name
                    }
                }
            }
            is ParadoxLocalisationParameterElement -> {
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("localisation.description.parameter")
                    else -> element.name
                }
            }
            is ParadoxDynamicValueElement -> {
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> PlsBundle.message("script.description.dynamicValue")
                    UsageViewNodeTextLocation.INSTANCE -> element.name + ": " + element.dynamicValueTypes.joinToString(" | ")
                    else -> element.name
                }
            }
            else -> null
        }
    }

    override fun getHelpId(psiElement: PsiElement): String {
        return "reference.dialogs.findUsages.other"
    }

    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return when (element) {
            is ParadoxLocalisationProperty -> element.localisationInfo != null
            is ParadoxParameterElement -> true
            is ParadoxDynamicValueElement -> true
            else -> false
        }
    }

    override fun getWordsScanner(): WordsScanner {
        return ParadoxLocalisationWordScanner()
    }
}
