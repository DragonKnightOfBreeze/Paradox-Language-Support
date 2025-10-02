package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.ParadoxTemplateExpressionResolverImpl

/**
 * 模版表达式。
 *
 * 说明：
 * - 对应的规则数据类型为 [CwtDataTypes.TemplateExpression]。
 */
interface ParadoxTemplateExpression : ParadoxComplexExpression {
    interface Resolver {
        fun resolve(expressionString: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxTemplateExpression?
    }

    companion object : Resolver by ParadoxTemplateExpressionResolverImpl()
}
