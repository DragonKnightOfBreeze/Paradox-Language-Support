package icu.windea.pls.lang.findUsages

import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.lang.psi.ParadoxElementDescriptionService
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptElementDescriptionService
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxScriptFindUsagesProvider : FindUsagesProvider {
    override fun canFindUsagesFor(element: PsiElement): Boolean {
        return when (element) {
            is ParadoxScriptScriptedVariable -> true
            is ParadoxScriptProperty -> true
            is LightElementBase -> element.language === ParadoxScriptLanguage
            else -> false
        }
    }

    override fun getWordsScanner() = ParadoxScriptWordScanner()

    override fun getHelpId(psiElement: PsiElement) = "reference.dialogs.findUsages.other"

    override fun getType(element: PsiElement): String {
        ParadoxElementDescriptionService.getType(element)?.let { return it }
        ParadoxScriptElementDescriptionService.getType(element)?.let { return it }
        return ""
    }

    override fun getDescriptiveName(element: PsiElement): String {
        ParadoxElementDescriptionService.getName(element)?.let { return it }
        ParadoxScriptElementDescriptionService.getName(element)?.let { return it }
        return ""
    }

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String {
        ParadoxElementDescriptionService.getNodeText(element)?.let { return it }
        ParadoxScriptElementDescriptionService.getNodeText(element)?.let { return it }
        return ""
    }
}
