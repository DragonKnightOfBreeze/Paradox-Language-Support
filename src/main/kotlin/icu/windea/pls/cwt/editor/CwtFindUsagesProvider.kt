package icu.windea.pls.cwt.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.util.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.cwt.psi.*

class CwtFindUsagesProvider : FindUsagesProvider, ElementDescriptionProvider {
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
        if (element.elementType == CwtElementTypes.LEFT_BRACE) {
            return when (location) {
                UsageViewTypeLocation.INSTANCE -> PlsBundle.message("cwt.description.block")
                else -> PlsConstants.Strings.blockFolder
            }
        }
        return when (element) {
            is CwtProperty -> {
                val configType = CwtConfigManager.getConfigType(element)?.takeIf { it.isReference }
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> configType?.description ?: PlsBundle.message("cwt.description.property")
                    else -> CwtConfigManager.getConfigType(element)?.getShortName(element.name) ?: element.name
                }
            }
            is CwtString -> {
                val configType = element.configType?.takeIf { it.isReference }
                when (location) {
                    UsageViewTypeLocation.INSTANCE -> configType?.description ?: PlsBundle.message("cwt.description.value")
                    else -> element.configType?.getShortName(element.name) ?: element.name
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
            is CwtProperty -> true
            is CwtString -> true
            else -> false
        }
    }

    override fun getWordsScanner(): WordsScanner {
        return CwtWordScanner()
    }
}
