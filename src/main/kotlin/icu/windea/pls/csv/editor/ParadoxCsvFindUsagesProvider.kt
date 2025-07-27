package icu.windea.pls.csv.editor

import com.intellij.lang.cacheBuilder.*
import com.intellij.lang.findUsages.*
import com.intellij.psi.*
import com.intellij.refactoring.util.*
import com.intellij.usageView.*
import icu.windea.pls.*
import icu.windea.pls.csv.psi.*

class ParadoxCsvFindUsagesProvider : FindUsagesProvider, ElementDescriptionProvider {
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
        if(element !is ParadoxCsvColumn) return null
        return when (location) {
            UsageViewTypeLocation.INSTANCE -> PlsBundle.message("csv.description.column")
            else -> element.name
        }
    }

    override fun getHelpId(psiElement: PsiElement): String {
        return "reference.dialogs.findUsages.other"
    }

    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return element is ParadoxCsvColumn
    }

    override fun getWordsScanner(): WordsScanner {
        return ParadoxCsvWordScanner()
    }
}
