package icu.windea.pls.lang.codeInsight

import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.model.*

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
