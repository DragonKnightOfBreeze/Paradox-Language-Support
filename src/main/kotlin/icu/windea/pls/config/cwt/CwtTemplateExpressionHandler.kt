package icu.windea.pls.config.cwt

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.expression.*

object CwtTemplateExpressionHandler {
    @JvmStatic
    fun extract(expression: CwtTemplateExpression, referenceName: String): String {
        if(expression.referenceExpressions.size != 1) throw IllegalStateException()
        return buildString {
            for(snippetExpression in expression.snippetExpressions) {
                when(snippetExpression.type) {
                    CwtDataType.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceName)
                }
            }
        }
    }
    
    @JvmStatic
    fun extract(templateExpression: CwtTemplateExpression, referenceNames: Map<CwtDataExpression, String>): String {
        if(templateExpression.referenceExpressions.size != referenceNames.size) throw IllegalStateException()
        return buildString {
            for(snippetExpression in templateExpression.snippetExpressions) {
                when(snippetExpression.type) {
                    CwtDataType.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceNames.getValue(snippetExpression))
                }
            }
        }
    }
    
    @JvmStatic
    fun resolve(expression: CwtTemplateExpression, text: String, configGroup: CwtConfigGroup): PsiElement? {
        return null
        //TODO
    }
    
    @JvmStatic
    fun processResolveResult(expression: CwtTemplateExpression, configGroup: CwtConfigGroup, processor: Processor<ParadoxTemplateExpression>) {
        //TODO
    }
}