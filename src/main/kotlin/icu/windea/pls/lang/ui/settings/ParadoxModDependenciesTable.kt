package icu.windea.pls.lang.ui.settings

import com.intellij.ide.CopyProvider
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.ui.PopupHandler
import com.intellij.ui.SpeedSearchComparator
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.TextTransferable
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.registerDoubleClickListener
import icu.windea.pls.lang.actions.ChronicleActionPlaces
import icu.windea.pls.lang.settings.ChronicleProfilesSettings
import icu.windea.pls.lang.settings.ParadoxGameOrModSettingsState
import icu.windea.pls.lang.settings.ParadoxModDependencySettingsState
import icu.windea.pls.lang.settings.ParadoxModSettingsState
import javax.swing.JPanel

class ParadoxModDependenciesTable(
    val model: ParadoxModDependenciesTableModel
) : JBTable(model), CopyProvider {
    /**
     * 得到选中项。
     */
    fun getSelectedItem(): ParadoxModDependencySettingsState? {
        val selectedRow = selectedRow
        val item = model.getItem(convertRowIndexToModel(selectedRow))
        return item
    }

    /**
     * 判断模组依赖表格中的最后一个是否是当前模组自身。
     */
    fun isCurrentAtLast(): Boolean {
        if (rowCount == 0) return false
        val currentModDirectory = model.settings.castOrNull<ParadoxModSettingsState>()?.modDirectory
        if (currentModDirectory == null) return false
        val lastRow = model.getItem(rowCount - 1)
        val lastModDirectory = lastRow.modDirectory
        return currentModDirectory == lastModDirectory
    }

    /**
     * 添加模组依赖到表格中合适的位置。
     */
    fun addModDependencies(newModDependencies: List<ParadoxModDependencySettingsState>) {
        // 如果最后一个模组依赖是当前模组自身，需要插入到它之前，否则直接添加到最后
        val isCurrentAtLast = isCurrentAtLast()
        val position = if (isCurrentAtLast) model.rowCount - 1 else model.rowCount
        if (newModDependencies.isNotEmpty()) {
            // 插入到表格
            model.insertRows(position, newModDependencies)
            // 选中刚刚添加的所有模组依赖
            setRowSelectionInterval(position, position + newModDependencies.size - 1)
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun performCopy(dataContext: DataContext) {
        // 允许复制选中的内容
        val text = selectedRows.joinToString("\n") {
            val item = model.getItem(convertRowIndexToModel(it))
            item.name + "\t" + item.version + "\t" + item.supportedVersion
        }
        CopyPasteManager.getInstance().setContents(TextTransferable(text as CharSequence))
    }

    override fun isCopyEnabled(dataContext: DataContext) = true

    override fun isCopyVisible(dataContext: DataContext) = selectedRowCount > 0

    companion object {
        // com.intellij.openapi.roots.ui.configuration.classpath.ClasspathPanelImpl.createTableWithButtons

        @JvmStatic
        fun createPanel(project: Project, settings: ParadoxGameOrModSettingsState, modDependencies: MutableList<ParadoxModDependencySettingsState>): JPanel {
            val tableModel = ParadoxModDependenciesTableModel(settings, modDependencies)
            val table = ParadoxModDependenciesTable(tableModel)
            table.setShowGrid(false)

            // 配置每一列
            val fontMetrics = table.getFontMetrics(table.font)
            val headerGap = JBUIScale.scale(20)

            // 调整尺寸
            run {
                val columnInfo = ParadoxModDependenciesTableModel.EnabledItem
                val column = table.columnModel.getColumn(columnInfo.columnIndex)
                val columnWidth = fontMetrics.stringWidth(columnInfo.name) + headerGap
                column.preferredWidth = columnWidth
                column.minWidth = columnWidth
            }
            run {
                val columnInfo = ParadoxModDependenciesTableModel.NameItem
                val column = table.columnModel.getColumn(columnInfo.columnIndex)
                column.preferredWidth = column.maxWidth
            }
            run {
                val columnInfo = ParadoxModDependenciesTableModel.VersionItem
                val column = table.columnModel.getColumn(columnInfo.columnIndex)
                val columnWidth = fontMetrics.stringWidth(columnInfo.name) + headerGap
                column.preferredWidth = columnWidth
                column.minWidth = columnWidth
            }
            run {
                val columnInfo = ParadoxModDependenciesTableModel.SupportedVersionItem
                val column = table.columnModel.getColumn(columnInfo.columnIndex)
                val columnWidth = fontMetrics.stringWidth(columnInfo.name) + headerGap
                column.preferredWidth = columnWidth
                column.minWidth = columnWidth
            }

            // 快速搜索
            TableSpeedSearch.installOn(table) { e ->
                val element = e as ParadoxModDependencySettingsState
                val modDirectory = element.modDirectory.orEmpty()
                val modDescriptorSettings = ChronicleProfilesSettings.getInstance().state.modDescriptorSettings.getValue(modDirectory)
                modDescriptorSettings.name.orEmpty()
            }.apply { comparator = SpeedSearchComparator(false) }

            // 双击打开模组依赖信息对话框
            table.registerDoubleClickListener action@{
                if (table.selectedRowCount != 1) return@action
                val selectedRow = table.selectedRow
                val item = tableModel.getItem(table.convertRowIndexToModel(selectedRow))
                ParadoxModDependencySettingsDialog(project, item, table).show()
            }

            // 创建工具栏

            val addButtonRunnable = ParadoxModDependenciesToolbarActions.AddAction(project, table)
            val editButton = ParadoxModDependenciesToolbarActions.EditAction(project, table)
            val enableAllButton = ParadoxModDependenciesToolbarActions.EnableAllAction(project, table)
            val disableAllButton = ParadoxModDependenciesToolbarActions.DisableAllAction(project, table)
            val importButton = ParadoxModDependenciesToolbarActions.ImportAction(project, table)
            val exportButton = ParadoxModDependenciesToolbarActions.ExportAction(project, table)

            // 这里我们需要保证排序正确（基于表格中的顺序）
            // 始终将模组放到自身的模组依赖列表中，其排序可以调整
            // add, remove, move up, move down, edit, import, export
            val panel = ToolbarDecorator.createDecorator(table)
                .setAddAction(addButtonRunnable)
                .setRemoveActionUpdater updater@{
                    // 不允许移除模组自身对应的模组依赖配置
                    val selectedRow = table.selectedRows.singleOrNull() ?: return@updater true
                    tableModel.canRemoveRow(table.convertRowIndexToModel(selectedRow))
                }
                .addExtraAction(editButton)
                .addExtraAction(enableAllButton)
                .addExtraAction(disableAllButton)
                .addExtraAction(importButton)
                .addExtraAction(exportButton)
                .createPanel()
            val addButton = ToolbarDecorator.findAddButton(panel)!!
            val removeButton = ToolbarDecorator.findRemoveButton(panel)!!

            // 右键弹出菜单，提供一些动作选项
            val actionGroup = DefaultActionGroup()
            actionGroup.addAction(addButton)
            actionGroup.addAction(removeButton)
            actionGroup.addAction(enableAllButton)
            actionGroup.addAction(disableAllButton)
            actionGroup.addAction(editButton)
            actionGroup.addAction(importButton)
            actionGroup.addAction(exportButton)
            actionGroup.addSeparator()
            actionGroup.addAction(ParadoxModDependenciesPopupActions.CopyModPathAction(table))
            actionGroup.addAction(ParadoxModDependenciesPopupActions.CopyModPageUrlAction(table))
            actionGroup.addAction(ParadoxModDependenciesPopupActions.OpenModPathAction(table))
            actionGroup.addAction(ParadoxModDependenciesPopupActions.OpenModPageInSteamAction(table))
            actionGroup.addAction(ParadoxModDependenciesPopupActions.OpenModPageInSteamWebsiteAction(table))
            PopupHandler.installPopupMenu(table, actionGroup, ChronicleActionPlaces.MOD_DEPENDENCIES_POPUP)
            return panel
        }
    }
}
