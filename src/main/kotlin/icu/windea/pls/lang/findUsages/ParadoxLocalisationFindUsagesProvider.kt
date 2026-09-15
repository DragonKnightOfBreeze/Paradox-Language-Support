package icu.windea.pls.lang.findUsages

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.lang.psi.ParadoxElementDescriptionService
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementDescriptionService
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

// com.intellij.lang.java.JavaFindUsagesProvider
// org.jetbrains.kotlin.idea.findUsages.KotlinFindUsagesProvider
// org.jetbrains.kotlin.idea.base.searching.usages.KotlinFindUsagesProviderBase

class ParadoxLocalisationFindUsagesProvider : FindUsagesProvider {
    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return when (element) {
            is ParadoxLocalisationProperty -> true
            is LightElementBase -> element.language === ParadoxLocalisationLanguage
            else -> false
        }
    }

    override fun getWordsScanner() = ParadoxLocalisationWordScanner()

    override fun getHelpId(psiElement: PsiElement) = "reference.dialogs.findUsages.other"

    override fun getType(element: PsiElement): String {
        ParadoxElementDescriptionService.getType(element)?.let { return it }
        ParadoxLocalisationElementDescriptionService.getType(element)?.let { return it }
        return ""
    }

    override fun getDescriptiveName(element: PsiElement): String {
        ParadoxElementDescriptionService.getName(element)?.let { return it }
        ParadoxLocalisationElementDescriptionService.getName(element)?.let { return it }
        return ""
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        ParadoxElementDescriptionService.getNodeText(element)?.let { return it }
        ParadoxLocalisationElementDescriptionService.getNodeText(element)?.let { return it }
        return ""
    }
}
