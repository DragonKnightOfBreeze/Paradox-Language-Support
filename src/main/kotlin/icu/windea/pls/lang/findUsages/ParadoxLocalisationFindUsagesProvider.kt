package icu.windea.pls.lang.findUsages

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.lang.psi.ParadoxPsiDescriptionService
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiDescriptionService

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
        ParadoxPsiDescriptionService.getType(element)?.let { return it }
        ParadoxLocalisationPsiDescriptionService.getType(element)?.let { return it }
        return ""
    }

    override fun getDescriptiveName(element: PsiElement): String {
        ParadoxPsiDescriptionService.getName(element)?.let { return it }
        ParadoxLocalisationPsiDescriptionService.getName(element)?.let { return it }
        return ""
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        ParadoxPsiDescriptionService.getNodeText(element)?.let { return it }
        ParadoxLocalisationPsiDescriptionService.getNodeText(element)?.let { return it }
        return ""
    }
}
