package icu.windea.pls.lang.codeInsight

import com.intellij.codeInsight.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.localisation.psi.*

class ParadoxTargetElementEvaluator: TargetElementEvaluatorEx2() {
    override fun getNamedElement(element: PsiElement): PsiElement? {
        return when (element.elementType) {
            //用于正确地显示本地化传入参数的快速文档
            ParadoxLocalisationElementTypes.ARGUMENT_TOKEN -> element.parent as? ParadoxLocalisationArgument
            else -> null
        }
    }
}
