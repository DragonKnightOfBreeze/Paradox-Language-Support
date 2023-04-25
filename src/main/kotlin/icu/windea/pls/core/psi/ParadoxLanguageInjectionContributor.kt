package icu.windea.pls.core.psi

import com.intellij.lang.injection.general.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.localisation.psi.*

class ParadoxLanguageInjectionContributor: LanguageInjectionContributor {
    companion object {
        const val LOCALISATION_COMMAND_EXPRESSION_PREFIX = "lc:"
    }
    
    override fun getInjection(context: PsiElement): Injection? {
        return when(context) {
            is ParadoxComplexExpressionElement -> getComplexExpressionInjection(context)
            else -> null
        } 
    }
    
    private fun getComplexExpressionInjection(context: ParadoxComplexExpressionElement): Injection? {
        return when(context) {
            is ParadoxLocalisationCommandExpression -> {
                handleComplexExpressionInjectionHost(context)
                val prefix = LOCALISATION_COMMAND_EXPRESSION_PREFIX
                SimpleInjection(context.language, prefix, "", null)
            }
            else ->  null
        }
    }
    
    private fun handleComplexExpressionInjectionHost(context: ParadoxComplexExpressionElement) {
        InjectionUtils.enableInjectLanguageAction(context, false)
    }
}