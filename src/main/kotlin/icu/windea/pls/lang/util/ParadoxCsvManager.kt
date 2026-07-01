package icu.windea.pls.lang.util

import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.util.ComputedModificationTracker
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvFile
import icu.windea.pls.csv.psi.ParadoxCsvHeader
import icu.windea.pls.csv.psi.ParadoxCsvRowElement
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.resolve.ParadoxCsvService

object ParadoxCsvManager {
    object Keys : KeyRegistry() {
        val cachedRowConfig by registerKey<CachedValue<CwtRowConfig>>(Keys)
    }

    fun getRowConfig(file: ParadoxCsvFile): CwtRowConfig? {
        // from cache
        // when the file content changes, the cache here does not need to be refreshed
        return CachedValuesManager.getCachedValue(file, Keys.cachedRowConfig) {
            val value = ParadoxCsvService.resolveRowConfig(file)
            value.withDependencyItems(ComputedModificationTracker { file.fileInfo })
        }
    }

    fun getRowConfig(element: ParadoxCsvRowElement): CwtRowConfig? {
        val file = element.containingFile?.castOrNull<ParadoxCsvFile>() ?: return null
        return getRowConfig(file)
    }

    fun getRowConfig(element: ParadoxCsvColumn): CwtRowConfig? {
        val file = element.containingFile?.castOrNull<ParadoxCsvFile>() ?: return null
        return getRowConfig(file)
    }

    fun getColumnConfig(element: ParadoxCsvColumn, rowConfig: CwtRowConfig): CwtPropertyConfig? {
        return ParadoxCsvService.getColumnConfig(element, rowConfig)
    }

    fun getColumnConfig(element: ParadoxCsvColumn): CwtPropertyConfig? {
        val rowConfig = getRowConfig(element) ?: return null
        return getColumnConfig(element, rowConfig)
    }

    fun isMatchedColumnConfig(column: ParadoxCsvColumn, columnConfig: CwtPropertyConfig): Boolean {
        return ParadoxCsvService.isMatchedColumnConfig(column, columnConfig)
    }

    @Deprecated("")
    fun getExpectedHeaderColumnSize(element: ParadoxCsvHeader): Int {
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
}
