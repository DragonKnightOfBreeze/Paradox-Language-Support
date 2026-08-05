package icu.windea.pls.ep.config.configExpression

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression

/**
 * 提供对数据表达式的支持。
 *
 * 例如，决定数据表达式的实际解析逻辑。
 *
 * @see CwtDataExpression
 */
interface CwtDataExpressionSupport {
    /**
     * 尝试解析数据表达式。
     */
    fun resolve(expressionString: String, role: CwtDataExpressionRole): CwtDataExpression?

    /**
     * 尝试解析模板表达式（[ParadoxTemplateExpression]）中作为片段的数据表达式。
     */
    fun resolveTemplate(expressionString: String): CwtDataExpression? = null

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<CwtDataExpressionSupport>("icu.windea.pls.dataExpressionSupport")
    }
}
