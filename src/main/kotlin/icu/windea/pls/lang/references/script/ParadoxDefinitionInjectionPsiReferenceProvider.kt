package icu.windea.pls.lang.references.script

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.parentProperty

/**
 * 解析定义注入表达式中的引用（模式规则、注入目标）。
 */
class ParadoxDefinitionInjectionPsiReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        if (element !is ParadoxScriptPropertyKey) return PsiReference.EMPTY_ARRAY
        val property = element.parentProperty ?: return PsiReference.EMPTY_ARRAY
        val info = ParadoxDefinitionInjectionManager.getInfo(property) ?: return PsiReference.EMPTY_ARRAY
        val offset = ParadoxExpressionManager.getExpressionOffset(element)

        // 兼容目标为空或者目标定义的类型为空的情况，此时仅返回 `modeReference`

        if (info.mode.isEmpty()) return PsiReference.EMPTY_ARRAY
        val modeRange = TextRange.from(offset, info.mode.length)
        val modeReference = ParadoxDefinitionInjectionModePsiReference(element, modeRange, info)

        if (info.target.isNullOrEmpty() || info.type.isNullOrEmpty()) return arrayOf(modeReference)
        val targetRange = TextRange.from(offset + info.mode.length + 1, info.target.length)
        val targetReference = ParadoxDefinitionInjectionTargetPsiReference(element, targetRange, info)
        return arrayOf(modeReference, targetReference)
    }
}
