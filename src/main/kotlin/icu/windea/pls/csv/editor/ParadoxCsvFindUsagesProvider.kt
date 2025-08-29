package icu.windea.pls.csv.editor

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
import icu.windea.pls.csv.psi.ParadoxCsvColumn

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
