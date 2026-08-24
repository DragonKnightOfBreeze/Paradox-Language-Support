package icu.windea.pls.lang.codeInsight.annotated

import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.core.text.QuotePatterns
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.text.ParadoxCsv
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.model.type.ParadoxTypeResolver

object ParadoxCsvAnnotatedManager {
    // region Prefixes

    const val typePrefix = "@type"
    const val configExpressionPrefix = "@config_expression"

    // endregion

    // region Annotation Getters

    /**
     * 得到类型信息的注解。
     *
     * 格式：
     * - `## @type type_1;type_2`
     */
    fun getTypeAnnotation(element: ParadoxCsvColumnContainer): String? {
        if (element is ParadoxCsvHeader) return null // skip for header
        val columns = element.columnList.orNull() ?: return null
        val types = columns.map { column -> ParadoxTypeResolver.resolveExpressionType(column).text }
        return types.joinToString(";", "## $typePrefix ")
    }

    /**
     * 得到规则表达式信息的注解。
     *
     * 格式：
     * - `## @type expression_1;expression_2`
     */
    fun getConfigExpressionAnnotation(element: ParadoxCsvColumnContainer): String? {
        if (element is ParadoxCsvHeader) return null // skip for header
        val rowConfig = ParadoxConfigManager.getRowConfig(element) ?: return null
        val columns = element.columnList.orNull() ?: return null
        val configExpressions = columns.map { column ->
            val columnConfig = ParadoxConfigManager.getColumnConfig(column, rowConfig) ?: return@map FallbackStrings.unknown
            if (!ParadoxConfigManager.isMatchedColumnConfig(column, columnConfig)) return@map FallbackStrings.unknown // require matched
            columnConfig.value
        }
        val quotePattern = QuotePatterns.ParadoxCsv
        return configExpressions.joinToString(";", "## $configExpressionPrefix ") { it.quoteIfNeeded(quotePattern) }
    }

    // endregion
}
