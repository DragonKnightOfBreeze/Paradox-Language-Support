package icu.windea.pls.core.tool

import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.*
import com.intellij.ui.table.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import javax.swing.*

class ParadoxModDependencyAddDialog(
    private val project: Project,
    private val tableView: TableView<ParadoxModDependencySettingsState>,
    private val tableModel: ParadoxModDependenciesTableModel,
) : DialogWrapper(project, tableView, true, IdeModalityType.PROJECT) {
    val settings = tableModel.settings
    
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(settings.gameType ?: getSettings().defaultGameType)
    val modDirectoryProperty = graph.property("")
    
    val gameType by gameTypeProperty
    val modDirectory by modDirectoryProperty
    
    init {
        title = PlsBundle.message("mod.dependency.add")
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        return panel {
            row {
                //gameType
                label(PlsBundle.message("mod.dependency.add.gameType")).widthGroup("left")
                comboBox(ParadoxGameType.valueList)
                    .bindItem(gameTypeProperty)
                    .columns(18)
                    .enabled(false)
            }
            row {
                //modDirectory
                label(PlsBundle.message("mod.dependency.add.modDirectory")).widthGroup("left")
                val descriptor = ParadoxRootDirectoryDescriptor()
                    .withTitle(PlsBundle.message("mod.dependency.add.modDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypePropertyKey, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .bindText(modDirectoryProperty)
                    .columns(36)
                    .align(Align.FILL)
                    .validationOnApply { validateModDirectory() }
            }
        }
    }
    
    private fun ValidationInfoBuilder.validateModDirectory(): ValidationInfo? {
        val modDirectory = modDirectory.normalizeAbsolutePath()
        if(modDirectory.isEmpty()) return error(PlsBundle.message("mod.dependency.add.modDirectory.error.empty"))
        val path = modDirectory.toPathOrNull()
            ?: return error(PlsBundle.message("mod.dependency.add.modDirectory.error.1"))
        val rootFile = VfsUtil.findFile(path, false)?.takeIf { it.exists() }
            ?: return error(PlsBundle.message("mod.dependency.add.modDirectory.error.2"))
        val rootInfo = ParadoxCoreHandler.resolveRootInfo(rootFile)
        if(rootInfo !is ParadoxModRootInfo) {
            return error(PlsBundle.message("mod.dependency.add.modDirectory.error.3"))
        }
        val currentModDirectory = settings.castOrNull<ParadoxModDependencySettingsState>()?.modDirectory
        if(currentModDirectory != null && modDirectory == currentModDirectory) {
            return error(PlsBundle.message("mod.dependency.add.modDirectory.error.4"))
        }
        return null
    }
    
    override fun doOKAction() {
        val modDirectory = modDirectory
        if(!tableModel.modDependencyDirectories.add(modDirectory)) return //忽略重复添加的模组依赖
        
        val newSettings = ParadoxModDependencySettingsState()
        newSettings.enabled = true
        newSettings.modDirectory = modDirectory
        
        //点击确定按钮后会弹出模组依赖配置对话框，以便预览模组配置，再次点击确定按钮才会添加到模组依赖列表 - 目前不这样做
        //val editDialog = ParadoxModDependencySettingsDialog(project, newSettings, this.contentPanel)
        //if(!editDialog.showAndGet()) return
        
        //如果最后一个模组依赖是当前模组自身，需要插入到它之前，否则直接添加到最后
        val isCurrentAtLast = tableModel.isCurrentAtLast()
        val position = if(isCurrentAtLast) tableModel.rowCount -1 else tableModel.rowCount
        tableModel.insertRow(position, newSettings)
        //选中刚刚添加的所有模组依赖
        tableView.setRowSelectionInterval(position, position)
        
        super.doOKAction()
    }
}
