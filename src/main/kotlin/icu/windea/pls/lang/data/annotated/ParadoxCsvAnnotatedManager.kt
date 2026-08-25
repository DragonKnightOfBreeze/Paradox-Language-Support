package icu.windea.pls.lang.data.annotated

import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.csv.psi.ParadoxCsvColumnContainer
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.model.type.ParadoxTypeResolver

@Optimized
object ParadoxAnnotatedInfoFactory {
    fun ofType(element: ParadoxCsvColumnContainer): ParadoxAnnotatedInfos.Type.ForColumns? {
        if (element is ParadoxCsvHeader) return null // skip for header
        val columns = element.columnList.orNull() ?: return null
        val types = columns.mapFast { ParadoxTypeResolver.resolveExpressionType(it) }
        return ParadoxAnnotatedInfos.Type.ForColumns(types)
    }

    fun getConfigExpressionAnnotation(element: ParadoxCsvColumnContainer): ParadoxAnnotatedInfos.ConfigExpression.ForColumns? {
        if (element is ParadoxCsvHeader) return null // skip for header
        val rowConfig = ParadoxConfigManager.getRowConfig(element) ?: return null
        val columns = element.columnList.orNull() ?: return null
        val configExpressions = columns.map { column ->
            val columnConfig = ParadoxConfigManager.getColumnConfig(column, rowConfig) ?: return@map FallbackStrings.unknown
            if (!ParadoxConfigManager.isMatchedColumnConfig(column, columnConfig)) return@map FallbackStrings.unknown // require matched
            columnConfig.valueExpression
        }
        return ParadoxAnnotatedInfos.ConfigExpression.ForColumns(configExpressions)
    }
}

