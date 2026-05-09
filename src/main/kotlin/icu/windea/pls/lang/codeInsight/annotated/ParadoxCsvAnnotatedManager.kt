package icu.windea.pls.lang.codeInsight.annotated

import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.lang.codeInsight.type.ParadoxTypeManager
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.model.ParadoxType

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
    fun getType(element: ParadoxCsvRowElement): String? {
        if (element is ParadoxCsvHeader) return null // skip for header
        val columns = element.columnList.orNull() ?: return null
        val types = columns.map { column ->
            val type = column.let { ParadoxTypeManager.getType(it) } ?: ParadoxType.Unknown
            type.id
        }
        return types.joinToString(";", "## $typePrefix ")
    }

    /**
     * 得到规则表达式信息的注解。
     *
     * 格式：
     * - `## @type expression_1;expression_2`
     */
    fun getConfigExpression(element: ParadoxCsvRowElement): String? {
        if (element is ParadoxCsvHeader) return null // skip for header
        val rowConfig = ParadoxCsvManager.getRowConfig(element) ?: return null
        val columns = element.columnList.orNull() ?: return null
        val configExpressions = columns.map { column ->
            val columnConfig = ParadoxCsvManager.getColumnConfig(column, rowConfig) ?: return@map FallbackStrings.unknown
            if (!ParadoxCsvManager.isMatchedColumnConfig(column, columnConfig)) return@map FallbackStrings.unknown // require matched
            columnConfig.value
        }
        return configExpressions.joinToString(";", "## $configExpressionPrefix ")
    }

    // endregion
}
