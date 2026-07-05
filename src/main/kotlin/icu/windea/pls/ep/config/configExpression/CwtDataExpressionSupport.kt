package icu.windea.pls.ep.config.configExpression

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.util.Processor
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.text.TextPattern
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression

/**
 * 提供对数据表达式的支持。
 *
 * 例如，决定数据表达式的实际解析逻辑。
 */
interface CwtDataExpressionSupport {
    /**
     * 尝试解析数据表达式。
     */
    fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression?

    /**
     * 尝试解析模板表达式（[ParadoxTemplateExpression]）中作为片段的数据表达式。
     */
    fun resolveTemplate(expressionString: String): CwtDataExpression? = resolve(expressionString, false)

    /**
     * 遍历支持的所有文本模式（[TextPattern]）。
     */
    fun processTextPatterns(consumer: Processor<TextPattern<*>>): Boolean = true

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<CwtDataExpressionSupport>("icu.windea.pls.dataExpressionSupport")
    }
}
