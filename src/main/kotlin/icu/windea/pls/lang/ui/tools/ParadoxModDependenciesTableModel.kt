package icu.windea.pls.lang.ui.tools

import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.ParadoxModDependencySettingsState
import icu.windea.pls.lang.settings.ParadoxModSettingsState

//com.intellij.openapi.roots.ui.configuration.classpath.ClasspathTableModel

class ParadoxModDependenciesTableModel(
    val settings: ParadoxGameOrModSettingsState,
    val modDependencies: MutableList<ParadoxModDependencySettingsState>
) : ListTableModel<ParadoxModDependencySettingsState>(
    arrayOf(EnabledItem, NameItem, VersionItem, SupportedVersionItem),
    modDependencies
) {
    val modDependencyDirectories = modDependencies.mapTo(mutableSetOf()) { it.modDirectory.orEmpty() }

    fun isCurrentAtLast(): Boolean {
        if (rowCount == 0) return false
        val currentModDirectory = settings.castOrNull<ParadoxModSettingsState>()?.modDirectory
        if (currentModDirectory == null) return false
        val lastRow = getItem(rowCount - 1)
        val lastModDirectory = lastRow.modDirectory
        return currentModDirectory == lastModDirectory
    }

    override fun removeRow(idx: Int) {
        //不允许移除模组自身对应的模组依赖配置
        if (!canRemoveRow(idx)) return
        modDependencyDirectories.remove(getItem(idx).modDirectory.orEmpty())
        super.removeRow(idx)
    }

    fun canRemoveRow(idx: Int): Boolean {
        if (settings !is ParadoxModSettingsState) return true
        if (settings.modDirectory.isNullOrEmpty()) return true
        val item = getItem(idx)
        return item.modDirectory != settings.modDirectory
    }

    fun insertRows(index: Int, items: Collection<ParadoxModDependencySettingsState>) {
        modDependencies.addAll(index, items)
        if (modDependencies.isNotEmpty()) {
            fireTableRowsInserted(index - items.size, index - 1)
        }
    }

    //注意这里的排序并不会实际改变modDependencies中模组依赖的排序

    object EnabledItem : ColumnInfo<ParadoxModDependencySettingsState, Boolean>(PlsBundle.message("mod.dependencies.column.name.enabled")) {
        const val columnIndex = 0

        override fun valueOf(item: ParadoxModDependencySettingsState): Boolean {
            return item.enabled
        }

        override fun setValue(item: ParadoxModDependencySettingsState, value: Boolean) {
            item.enabled = value
        }

        override fun isCellEditable(item: ParadoxModDependencySettingsState): Boolean {
            return true
        }

        override fun getColumnClass(): Class<*> {
            return Boolean::class.java
        }
    }

    object NameItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.dependencies.column.name.name")) {
        const val columnIndex = 1

        private val _comparator = compareBy<ParadoxModDependencySettingsState> { item -> item.name.orEmpty() }

        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            return item.name.orEmpty()
        }

        override fun getComparator(): Comparator<ParadoxModDependencySettingsState> {
            return _comparator
        }
    }

    object VersionItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.dependencies.column.version.name")) {
        const val columnIndex = 2

        private val _comparator = compareBy<ParadoxModDependencySettingsState> { item -> item.version.orEmpty() }

        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            return item.version.orEmpty()
        }

        override fun getComparator(): Comparator<ParadoxModDependencySettingsState> {
            return _comparator
        }
    }

    object SupportedVersionItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.dependencies.column.supportedVersion.name")) {
        const val columnIndex = 3

        private val _comparator = compareBy<ParadoxModDependencySettingsState> { item -> item.supportedVersion.orEmpty() }

        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            return item.supportedVersion.orEmpty()
        }

        override fun getComparator(): Comparator<ParadoxModDependencySettingsState> {
            return _comparator
        }
    }
}
