package icu.windea.pls.lang.references.script

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isResolvableExpression

class ParadoxScriptExpressionPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        if (element !is ParadoxScriptExpressionElement) return PsiReference.EMPTY_ARRAY
        if (!element.isResolvableExpression()) return PsiReference.EMPTY_ARRAY //#131

        //尝试解析为复杂枚举值声明
        run {
            if (element !is ParadoxScriptStringExpressionElement) return@run
            val complexEnumValueInfo = ParadoxComplexEnumValueManager.getInfo(element) ?: return@run
            val textRange = ParadoxExpressionManager.getExpressionTextRange(element) //unquoted text
            val reference = ParadoxComplexEnumValuePsiReference(element, textRange, complexEnumValueInfo, element.project)
            return arrayOf(reference)
        }

        return ParadoxExpressionManager.getExpressionReferences(element)
    }
}
