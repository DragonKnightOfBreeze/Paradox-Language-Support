package icu.windea.pls.core.settings

import com.intellij.ide.*
import com.intellij.openapi.application.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.*
import com.intellij.ui.table.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.listeners.*
import icu.windea.pls.core.ui.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import java.awt.event.*
import javax.swing.*


class ParadoxModSettingsDialog(
    val project: Project,
    val settings: ParadoxModSettingsState
) : DialogWrapper(project, true) {
    val descriptorSettings = getAllModSettings().descriptorSettings.getValue(settings.modDirectory.orEmpty())
    
    val oldGameType = settings.gameType ?: getSettings().defaultGameType
    
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(settings.gameType ?: getSettings().defaultGameType)
        .apply { afterChange { settings.gameType = it } }
    val gameDirectoryProperty = graph.property(settings.gameDirectory.orEmpty())
        .apply { afterChange { settings.gameDirectory = it } }
    
    var gameType by gameTypeProperty
    var gameDirectory by gameDirectoryProperty
    
    val modDependenciesTableModel: ParadoxModDependenciesTableModel
    
    init {
        title = PlsBundle.message("mod.settings")
        handleModSettings()
        modDependenciesTableModel = ParadoxModDependenciesTableModel(this)
        init()
    }
    
    //name (readonly)
    //version (readonly) supportedVersion? (readonly)
    //comment
    
    //game type (combobox)
    //game directory (filepath text field)
    //mod directory (filepath text field)
    
    //mod dependencies (foldable group)
    //  mod dependencies table
    //  actions: add (select mod directory & import from file), remove, move up, move down, edit
    //  columns: order (int text field), ~~icon (thumbnail)~~, name (readonly), version (readonly), supportedVersion (readonly)
    //  when add or edit a column: show edit dialog (+ mod directory)
    
    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                //name
                label(PlsBundle.message("mod.settings.name")).widthGroup("mod.settings.left")
                textField()
                    .text(descriptorSettings.name.orEmpty())
                    .align(Align.FILL)
                    .columns(36)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.settings.version")).widthGroup("mod.settings.left")
                textField()
                    .text(descriptorSettings.version.orEmpty())
                    .align(Align.FILL)
                    .columns(18)
                    .enabled(false)
                    .visible(descriptorSettings.version.orEmpty().isNotEmpty())
                //supportedVersion
                label(PlsBundle.message("mod.settings.supportedVersion")).widthGroup("mod.settings.right")
                textField()
                    .text(descriptorSettings.supportedVersion.orEmpty())
                    .align(Align.FILL)
                    .columns(18)
                    .enabled(false)
                    .visible(descriptorSettings.supportedVersion.orEmpty().isNotEmpty())
            }
            row {
                comment(PlsBundle.message("mod.settings.comment"))
            }
            row {
                //gameType
                label(PlsBundle.message("mod.settings.gameType")).widthGroup("mod.settings.left")
                comboBox(ParadoxGameType.valueList)
                    .bindItem(gameTypeProperty)
                    .align(Align.FILL)
                    .columns(18)
                    .onApply { settings.gameType = gameTypeProperty.get() } //set game type to non-default on apply
                //quickSelectGameDirectory
                link(PlsBundle.message("mod.settings.quickSelectGameDirectory")) { quickSelectGameDirectory() }
            }
            row {
                //gameDirectory
                label(PlsBundle.message("mod.settings.gameDirectory")).widthGroup("mod.settings.left")
                val descriptor = ParadoxRootDirectoryDescriptor()
                    .withTitle(PlsBundle.message("mod.settings.gameDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypePropertyKey, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .bindText(gameDirectoryProperty)
                    .align(Align.FILL)
                    .columns(36)
                    .validationOnApply { validateGameDirectory() }
            }
            row {
                //modDirectory
                label(PlsBundle.message("mod.settings.modDirectory")).widthGroup("mod.settings.left")
                val descriptor = ParadoxRootDirectoryDescriptor()
                    .withTitle(PlsBundle.message("mod.settings.modDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypePropertyKey, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .text(settings.modDirectory.orEmpty())
                    .align(Align.FILL)
                    .columns(36)
                    .enabled(false)
            }
            
            collapsibleGroup(PlsBundle.message("mod.settings.modDependencies"), false) {
                row {
                    cell(createModDependenciesPanel()).align(Align.CENTER)
                }
            }
        }
    }
    
    //com.intellij.openapi.roots.ui.configuration.classpath.ClasspathPanelImpl.createTableWithButtons
    
    private fun createModDependenciesPanel(): JPanel {
        val tableView = createModDependenciesTableView()
        
        //这里我们需要保证排序正确（基于表格中你的顺序）
        //始终将模组放到自身的模组依赖列表中，其排序可以调整
        val panel = ToolbarDecorator.createDecorator(tableView)
            //TODO
            .createPanel()
        return panel
    }
    
    private fun createModDependenciesTableView(): TableView<ParadoxModDependencySettingsState> {
        val tableModel = modDependenciesTableModel
        val tableView = ParadoxModDependenciesTableView(tableModel)
        tableView.setFixedColumnWidth(ParadoxModDependenciesTableModel.SelectedItem.columnIndex, ParadoxModDependenciesTableModel.SelectedItem.name)
        //快速搜索
        object : SpeedSearchBase<TableView<ParadoxModDependencySettingsState>>(tableView) {
            override fun getSelectedIndex(): Int {
                return tableView.selectedRow
            }
            
            override fun getElementCount(): Int {
                return tableModel.rowCount
            }
            
            override fun getElementAt(viewIndex: Int): Any {
                return tableModel.getItem(tableView.convertRowIndexToModel(viewIndex))
            }
            
            override fun getElementText(element: Any): String {
                val modDirectory = (element as ParadoxModDependencySettingsState).modDirectory.orEmpty()
                val modDescriptorSettings = getAllModSettings().descriptorSettings.getValue(modDirectory)
                return modDescriptorSettings.name.orEmpty()
            }
            
            override fun selectElement(element: Any, selectedText: String) {
                val count = tableModel.rowCount
                for(row in 0 until count) {
                    if(element == tableModel.getItem(row)) {
                        val viewRow = tableView.convertRowIndexToView(row)
                        tableView.selectionModel.setSelectionInterval(viewRow, viewRow)
                        TableUtil.scrollSelectionToVisible(tableView)
                        break
                    }
                }
            }
        }
        //双击打开模组依赖信息对话框（仅显示，无法编辑）
        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                if(tableView.selectedRowCount != 1) return true
                val selectedRow = tableView.selectedRow
                val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
                ParadoxModDependencySettingsDialog(project, item).show()
                return true
            }
        }.installOn(tableView)
        //TODO 右键弹出菜单，提供一些操作项
        return tableView
    }
    
    private fun quickSelectGameDirectory() {
        val targetPath = getSteamGamePath(gameType.gameSteamId, gameType.gameName) ?: return
        gameDirectory = targetPath
    }
    
    private fun ValidationInfoBuilder.validateGameDirectory(): ValidationInfo? {
        //验证游戏目录是否合法
        //* 路径合法
        //* 路径对应的目录存在
        //* 路径是游戏目录（可以查找到对应的launcher-settings.json）
        val path = gameDirectory.toPathOrNull()
            ?: return error(PlsBundle.message("mod.settings.gameDirectory.error.1"))
        val rootFile = VfsUtil.findFile(path, false)?.takeIf { it.exists() }
            ?: return error(PlsBundle.message("mod.settings.gameDirectory.error.2"))
        val rootInfo = ParadoxCoreHandler.resolveRootInfo(rootFile)
        if(rootInfo?.rootType != ParadoxRootType.Game) {
            return error(PlsBundle.message("mod.settings.gameDirectory.error.3", gameType.description))
        }
        return null
    }
    
    private fun handleModSettings() {
        //如果需要，加上缺失的模组自身的模组依赖配置
        val modDependencies = settings.modDependencies
        if(modDependencies.find { it.modDirectory == settings.modDirectory } == null) {
            val newSettings = ParadoxModDependencySettingsState()
            newSettings.modDirectory = settings.modDirectory
            modDependencies.add(newSettings)
        }
    }
    
    override fun doOKAction() {
        super.doOKAction()
        
        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onChange(settings)
        if(oldGameType != settings.gameType) {
            messageBus.syncPublisher(ParadoxModGameTypeListener.TOPIC).onChange(settings)
        }
    }
}

//com.intellij.openapi.roots.ui.configuration.classpath.ClasspathTableModel

class ParadoxModDependenciesTableModel(
    dialog: ParadoxModSettingsDialog
) : ListTableModel<ParadoxModDependencySettingsState>() {
    init {
        columnInfos = arrayOf(SelectedItem, NameItem, VersionItem)
        items = dialog.settings.modDependencies
    }
    
    object SelectedItem : ColumnInfo<ParadoxModDependencySettingsState, Boolean>(PlsBundle.message("mod.settings.modDependencies.column.selected.name")) {
        const val columnIndex = 0
        
        override fun valueOf(item: ParadoxModDependencySettingsState): Boolean {
            return item.selected
        }
        
        override fun setValue(item: ParadoxModDependencySettingsState, value: Boolean) {
            item.selected = value
        }
        
        override fun isCellEditable(item: ParadoxModDependencySettingsState): Boolean {
            return true
        }
        
        override fun getColumnClass(): Class<*> {
            return Boolean::class.java
        }
    }
    
    object NameItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.settings.modDependencies.column.name.name")) {
        const val columnIndex = 1
        
        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            val descriptorSettings = getAllModSettings().descriptorSettings.getValue(item.modDirectory.orEmpty())
            return descriptorSettings.name.orEmpty()
        }
    }
    
    object VersionItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.settings.modDependencies.column.version.name")) {
        const val columnIndex = 2
        
        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            val descriptorSettings = getAllModSettings().descriptorSettings.getValue(item.modDirectory.orEmpty())
            return descriptorSettings.version.orEmpty()
        }
    }
}

class ParadoxModDependenciesTableView(
    tableModel: ParadoxModDependenciesTableModel
) : TableView<ParadoxModDependencySettingsState>(tableModel) {
    init {
        setShowGrid(false)
        cellSelectionEnabled = false
        selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        selectionModel.setSelectionInterval(0, 0)
        surrendersFocusOnKeystroke = true
    }
}

class ParadoxModDependencySettingsDialog(
    val project: Project,
    val settings: ParadoxModDependencySettingsState
) : DialogWrapper(project, true) {
    val descriptorSettings = getAllModSettings().descriptorSettings.getValue(settings.modDirectory.orEmpty())
    
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(descriptorSettings.gameType ?: getSettings().defaultGameType)
        .apply { afterChange { descriptorSettings.gameType = it } }
    
    init {
        title = PlsBundle.message("mod.dependency.settings")
        init()
    }
    
    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                //name
                label(PlsBundle.message("mod.dependency.settings.name")).widthGroup("mod.dependency.settings.left")
                textField()
                    .text(descriptorSettings.name.orEmpty())
                    .align(Align.FILL)
                    .columns(32)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.dependency.settings.version")).widthGroup("mod.dependency.settings.left")
                textField()
                    .text(descriptorSettings.version.orEmpty())
                    .align(Align.FILL)
                    .columns(16)
                    .enabled(false)
                    .visible(descriptorSettings.version.orEmpty().isNotEmpty())
                //supportedVersion
                label(PlsBundle.message("mod.settings.supportedVersion")).widthGroup("mod.dependency.settings.right")
                textField()
                    .text(descriptorSettings.supportedVersion.orEmpty())
                    .align(Align.FILL)
                    .columns(16)
                    .enabled(false)
                    .visible(descriptorSettings.supportedVersion.orEmpty().isNotEmpty())
            }
            row {
                comment(PlsBundle.message("mod.dependency.settings.comment"))
            }
            row {
                //gameType
                label(PlsBundle.message("mod.settings.gameType")).widthGroup("mod.dependency.settings.left")
                comboBox(ParadoxGameType.valueList)
                    .align(Align.FILL)
                    .columns(16)
                    .enabled(false)
            }
            row {
                //modDirectory
                label(PlsBundle.message("mod.settings.modDirectory")).widthGroup("mod.dependency.settings.left")
                val descriptor = ParadoxRootDirectoryDescriptor()
                    .withTitle(PlsBundle.message("mod.dependency.settings.modDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypePropertyKey, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .text(settings.modDirectory.orEmpty())
                    .align(Align.FILL)
                    .columns(32)
                    .enabled(false)
            }
        }
    }
    
    override fun createDefaultActions() {
        okAction.isEnabled = false
        cancelAction.putValue(Action.NAME, IdeBundle.message("action.close"))
    }
}