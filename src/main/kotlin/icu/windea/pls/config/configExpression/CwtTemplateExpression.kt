package icu.windea.pls.config.configExpression

import icu.windea.pls.config.configExpression.impl.CwtTemplateExpressionResolverImpl

// job_<job>_add
// xxx_value[xxx]_xxx

interface CwtTemplateExpression : CwtConfigExpression {
    val snippetExpressions: List<CwtDataExpression>
    val referenceExpressions: List<CwtDataExpression>

    interface Resolver {
        fun resolveEmpty(): CwtTemplateExpression
        fun resolve(expressionString: String): CwtTemplateExpression
    }

    companion object : Resolver by CwtTemplateExpressionResolverImpl()
}
