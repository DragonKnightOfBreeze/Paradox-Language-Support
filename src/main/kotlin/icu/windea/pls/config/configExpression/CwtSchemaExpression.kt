package icu.windea.pls.config.configExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configExpression.impl.CwtSchemaExpressionResolverImpl

interface CwtSchemaExpression : CwtConfigExpression {
    interface Constant : CwtSchemaExpression

    interface Template : CwtSchemaExpression {
        val pattern: String
        val parameterRanges: List<TextRange>
    }

    interface Type : CwtSchemaExpression {
        val name: String
    }

    interface Enum : CwtSchemaExpression {
        val name: String
    }

    interface Constraint : CwtSchemaExpression {
        val name: String
    }

    interface Resolver {
        fun resolveEmpty(): CwtSchemaExpression
        fun resolve(expressionString: String): CwtSchemaExpression
    }

    companion object : Resolver by CwtSchemaExpressionResolverImpl()
}
