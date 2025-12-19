package icu.windea.pls.ep.configExpression

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configExpression.CwtDataExpression

/**
 * 用于解析数据表达式。
 */
interface CwtDataExpressionResolver {
    /**
     * 得到解析结果。
     */
    fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtDataExpressionResolver>("icu.windea.pls.dataExpressionResolver")
    }
}
