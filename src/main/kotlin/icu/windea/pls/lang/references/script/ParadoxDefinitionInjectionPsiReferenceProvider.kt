package icu.windea.pls.lang.references.script

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.property

/**
 * 解析定义注入表达式中的引用（模式规则、注入目标）。
 */
class ParadoxDefinitionInjectionPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        if (element !is ParadoxScriptPropertyKey) return PsiReference.EMPTY_ARRAY
        val property = element.property ?: return PsiReference.EMPTY_ARRAY
        val info = ParadoxDefinitionInjectionManager.getInfo(property) ?: return PsiReference.EMPTY_ARRAY

        return PsiReference.EMPTY_ARRAY // TODO 2.1.0
    }
}
