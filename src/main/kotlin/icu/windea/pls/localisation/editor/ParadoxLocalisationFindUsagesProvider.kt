package icu.windea.pls.localisation.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.refactoring.util.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

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
                            when (localisationInfo.category) {
                                ParadoxLocalisationCategory.Normal -> PlsBundle.message("localisation.description.localisation")
                                ParadoxLocalisationCategory.Synced -> PlsBundle.message("localisation.description.syncedLocalisation")
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
