package icu.windea.pls.ep.config.configExpression

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.util.Processor
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.util.text.TextPattern

/**
 * 用于解析数据表达式。
 */
interface CwtDataExpressionResolver {
    /**
     * 得到解析结果。
     */
    fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression?

    /**
     * 遍历此解析器支持的所有文本模式。
     */
    fun processTextPatterns(consumer: Processor<TextPattern<*>>): Boolean = true

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtDataExpressionResolver>("icu.windea.pls.dataExpressionResolver")
    }
}
