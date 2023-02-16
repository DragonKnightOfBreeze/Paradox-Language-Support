package icu.windea.pls.core.settings

import com.intellij.openapi.application.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.*
import com.intellij.ui.table.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.listeners.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import javax.swing.*
import javax.swing.event.*
import javax.swing.table.*


class ParadoxModSettingsDialog(
    val project: Project,
    val modDirectory: String,
) : DialogWrapper(project, true) {
    val allModSettings = getAllModSettings()
    val modSettings = allModSettings.settings.getValue(modDirectory)
    val modDescriptorSettings = allModSettings.descriptorSettings.getValue(modDirectory)
    val modDependencies = modSettings.modDependencies.values.toList()
    
    val oldGameType = modSettings.gameType ?: getSettings().defaultGameType
    
    val graph = PropertyGraph()
    
    val gameTypeProperty = graph.property(modSettings.gameType ?: getSettings().defaultGameType)
        .apply { afterChange { modSettings.gameType = it } }
    val gameDirectoryProperty = graph.property(modSettings.gameDirectory.orEmpty())
        .apply { afterChange { modSettings.gameDirectory = it } }
    
    var gameType by gameTypeProperty
    var gameDirectory by gameDirectoryProperty
    
    val modDependenciesTableModel: ParadoxModDependenciesTableModel
    
    init {
        title = PlsBundle.message("mod.settings")
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
                    .text(modDescriptorSettings.name.orEmpty())
                    .align(Align.FILL)
                    .columns(32)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.settings.version")).widthGroup("mod.settings.left")
                textField()
                    .text(modDescriptorSettings.version.orEmpty())
                    .align(Align.FILL)
                    .columns(18)
                    .enabled(false)
                    .visible(modDescriptorSettings.version.orEmpty().isNotEmpty())
                //supportedVersion
                label(PlsBundle.message("mod.settings.supportedVersion")).widthGroup("mod.settings.right")
                textField()
                    .text(modDescriptorSettings.supportedVersion.orEmpty())
                    .align(Align.FILL)
                    .columns(18)
                    .enabled(false)
                    .visible(modDescriptorSettings.supportedVersion.orEmpty().isNotEmpty())
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
                    .onApply { modSettings.gameType = gameTypeProperty.get() } //set game type to non-default on apply
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
                    .text(modDirectory)
                    .align(Align.FILL)
                    .columns(36)
                    .enabled(false)
            }
            
            collapsibleGroup(PlsBundle.message("mod.settings.modDependencies"), false) {
                row { 
                    cell(createModDependenciesPanel())
                }
            }
        }
    }
    
    //com.intellij.openapi.roots.ui.configuration.classpath.ClasspathPanelImpl.createTableWithButtons
    
    private fun createModDependenciesPanel(): JPanel {
        val tableView = object : TableView<ParadoxModDependencySettingsState>(modDependenciesTableModel) {
            override fun editingStopped(e: ChangeEvent?) {
                super.editingStopped(e)
                repaint() // to update disabled cells background
            }
        }
        tableView.setShowGrid(false)
        tableView.cellSelectionEnabled = false
        tableView.selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        tableView.selectionModel.setSelectionInterval(0, 0)
        tableView.surrendersFocusOnKeystroke = true
        //这里我们需要保证排序正确（基于表格中你的顺序以及order属性的值）
        //TODO 始终将模组放到自身的模组依赖列表中，其排序可以调整
        val panel = ToolbarDecorator.createDecorator(tableView)
            //TODO
            .createPanel()
        return panel
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
    
    override fun doOKAction() {
        super.doOKAction()
    
        modSettings.modDependencies.clear()
        modDependencies.associateByTo(modSettings.modDependencies) { it.modDirectory.orEmpty() }
        
        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onChange(modSettings)
        if(oldGameType != modSettings.gameType) {
            messageBus.syncPublisher(ParadoxModGameTypeListener.TOPIC).onChange(modSettings)
        }
    }
}

//com.intellij.openapi.roots.ui.configuration.classpath.ClasspathTableModel

class ParadoxModDependenciesTableModel(
    val dialog: ParadoxModSettingsDialog
) : ListTableModel<ParadoxModDependencySettingsState>() {
    init {
        columnInfos = arrayOf(SelectedItem, OrderItem, NameItem, VersionItem)
        items = dialog.modDependencies
    }
    
    override fun exchangeRows(idx1: Int, idx2: Int) {
        val item1 = items[idx1]
        val item2 = items[idx2]
        item1.order = idx2 + 1
        item2.order = idx1 + 1
        super.exchangeRows(idx1, idx2)
    }
    
    object SelectedItem : ColumnInfo<ParadoxModDependencySettingsState, Boolean>(PlsBundle.message("mod.settings.modDependencies.column.selected.name")) {
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
    
    object OrderItem : ColumnInfo<ParadoxModDependencySettingsState, Int>(PlsBundle.message("mod.settings.modDependencies.column.order.name")) {
        override fun valueOf(item: ParadoxModDependencySettingsState): Int {
            return item.order
        }
        
        override fun setValue(item: ParadoxModDependencySettingsState, value: Int) {
            if(item.order == value) return
            item.order = value
            //TODO 重新排序
        }
        
        override fun getColumnClass(): Class<*> {
            return Int::class.java
        }
        
        override fun getRenderer(item: ParadoxModDependencySettingsState): TableCellRenderer {
            return DefaultTableCellRenderer()
        }
        
        override fun getEditor(item: ParadoxModDependencySettingsState): TableCellEditor {
            return DefaultCellEditor(object : JBTextField() {
                override fun setText(t: String) {
                    val v = t.trim()
                    if(v.toIntOrNull() == null) return
                    super.setText(t)
                }
            })
        }
    }
    
    object NameItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.settings.modDependencies.column.name.name")) {
        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            val descriptorSettings = getAllModSettings().descriptorSettings.getValue(item.modDirectory.orEmpty())
            return descriptorSettings.name.orEmpty()
        }
    }
    
    object VersionItem : ColumnInfo<ParadoxModDependencySettingsState, String>(PlsBundle.message("mod.settings.modDependencies.column.version.name")) {
        override fun valueOf(item: ParadoxModDependencySettingsState): String {
            val descriptorSettings = getAllModSettings().descriptorSettings.getValue(item.modDirectory.orEmpty())
            return descriptorSettings.version.orEmpty()
        }
    }
}