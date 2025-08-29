package icu.windea.pls.lang.codeInsight

import com.intellij.lang.ExpressionTypeProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 用于显示本地化的类型信息（`View > Type Info`）。
 */
class ParadoxLocalisationTypeProvider : ExpressionTypeProvider<PsiElement>() {
    override fun getExpressionsAt(elementAt: PsiElement): List<PsiElement> {
        return ParadoxLocalisationTypeManager.findTypedElements(elementAt)
    }

    override fun getInformationHint(element: PsiElement): String {
        val type = ParadoxLocalisationTypeManager.getType(element) ?: ParadoxLocalisationType.Normal
        return type.id
    }

    override fun getErrorHint(): String {
        return PlsBundle.message("no.expression.found")
    }
}
