package icu.windea.pls.lang.util

import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtRowConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.rows
import icu.windea.pls.config.util.CwtConfigManager
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.util.ComputedModificationTracker
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRow
import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.csv.psi.getHeaderColumn
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.ep.expression.ParadoxCsvExpressionMatcher
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.model.paths.ParadoxPath

object ParadoxCsvManager {
    object Keys : KeyRegistry() {
        val cachedRowConfig by createKey<CachedValue<CwtRowConfig>>(Keys)
    }

    private const val SEPARATOR = ';'

    fun getSeparator(): Char {
        return SEPARATOR
    }

    fun getRowConfig(file: ParadoxCsvFile): CwtRowConfig? {
        return doGetRowConfigFromCache(file)
    }

    fun getRowConfig(element: ParadoxCsvRowElement): CwtRowConfig? {
        val file = element.containingFile
        if (file !is ParadoxCsvFile) return null
        return doGetRowConfigFromCache(file)
    }

    private fun doGetRowConfigFromCache(file: ParadoxCsvFile): CwtRowConfig? {
        return CachedValuesManager.getCachedValue(file, Keys.cachedRowConfig) {
            val value = doGetRowConfig(file)
            val tracker = ComputedModificationTracker { file.fileInfo } //文件内容发生变化时，这里的缓存并不需要刷新
            value.withDependencyItems(tracker)
        }
    }

    private fun doGetRowConfig(file: ParadoxCsvFile): CwtRowConfig? {
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path
        val gameType = fileInfo.rootInfo.gameType
        val configGroup = PlsFacade.getConfigGroup(project, gameType)
        val rowConfig = getMatchedRowConfig(configGroup, path)
        return rowConfig
    }

    fun getMatchedRowConfig(configGroup: CwtConfigGroup, path: ParadoxPath): CwtRowConfig? {
        for (rowConfig in configGroup.rows.values) {
            if (!matchesRow(rowConfig, path)) continue
            return rowConfig
        }
        return null
    }

    fun matchesRow(rowConfig: CwtRowConfig, path: ParadoxPath?): Boolean {
        if (path != null) {
            if (!CwtConfigManager.matchesFilePathPattern(rowConfig, path)) return false
        }

        return true
    }

    fun getColumnConfig(column: ParadoxCsvColumn): CwtPropertyConfig? {
        val file = column.containingFile?.castOrNull<ParadoxCsvFile>() ?: return null
        val rowConfig = getRowConfig(file) ?: return null
        return getColumnConfig(column, rowConfig)
    }

    fun getColumnConfig(column: ParadoxCsvColumn, rowConfig: CwtRowConfig): CwtPropertyConfig? {
        val headerName = when {
            column.isHeaderColumn() -> column.name
            else -> column.getHeaderColumn()?.name
        }
        if (headerName.isNullOrEmpty()) return null
        return rowConfig.columns[headerName]
    }

    fun isMatchedColumnConfig(column: ParadoxCsvColumn, columnConfig: CwtPropertyConfig, matchOptions: Int = ParadoxExpressionMatcher.Options.Default): Boolean {
        if (column.isHeaderColumn()) {
            return column.value == columnConfig.key
        }
        val configExpression = columnConfig.valueConfig?.configExpression ?: return false
        return ParadoxCsvExpressionMatcher.matches(column, column.text, configExpression, columnConfig.configGroup).get(matchOptions)
    }

    fun computeHeaderColumnSize(element: ParadoxCsvHeader): Int {
        val columnList = element.columnList
        if (lastIsEndColumn(element, columnList)) return columnList.size - 1
        return columnList.size
    }

    private fun lastIsEndColumn(element: ParadoxCsvHeader, columnList: List<ParadoxCsvColumn>): Boolean {
        val lastColumn = columnList.lastOrNull() ?: return false
        val rowConfig = getRowConfig(element) ?: return false
        val name = lastColumn.name
        return name.isNotEmpty() && name == rowConfig.endColumn
    }

    fun computeColumnSize(element: ParadoxCsvRow): Int {
        val columnList = element.columnList
        return columnList.size
    }
}
