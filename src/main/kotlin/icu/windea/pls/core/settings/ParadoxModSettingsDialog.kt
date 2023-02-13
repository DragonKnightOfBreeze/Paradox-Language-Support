package icu.windea.pls.core.settings

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.listeners.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

class ParadoxModSettingsDialog(
    val project: Project,
    val modSettings: ParadoxModSettingsState,
): DialogWrapper(project, true) {
    val oldGameType = modSettings.gameType
    
    init {
        title = PlsBundle.message("mod.settings")
        init()
    }
    
    val graph = PropertyGraph()
    
    val gameTypeProperty = graph.property(modSettings.gameType)
        .apply { afterChange { modSettings.gameType = it } }
    val gameDirectoryProperty = graph.property(modSettings.gameDirectory.orEmpty())
        .apply { afterChange { modSettings.gameDirectory = it } }
    val modPathProperty = graph.property(modSettings.modPath.orEmpty())
        .apply { afterChange { modSettings.modPath = it } }
    
    val gameType by gameTypeProperty
    val gameDirectory by gameDirectoryProperty
    val modPath by modPathProperty
    
    //name (readonly)
    //version (readonly) supportedVersion? (readonly)
    //comment
    
    //game type (combobox)
    //game directory (filepath text field)
    //mod path (filepath text field)
    
    //mod dependencies (foldable group)
    //  mod dependencies table
    //  actions: add (select mod path & import from file), remove, move up, move down, edit
    //  columns: order (int text field), ~~icon (thumbnail)~~, name (readonly), version (readonly), supportedVersion (readonly)
    //  when add or edit a column: show edit dialog (+ mod path)
    
    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                //name
                label(PlsBundle.message("mod.settings.name")).widthGroup("mod.settings.left")
                textField()
                    .text(modSettings.name.orEmpty())
                    .align(Align.FILL)
                    .columns(32)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.settings.version")).widthGroup("mod.settings.left")
                textField()
                    .text(modSettings.version.orEmpty())
                    .align(Align.FILL)
                    .columns(18)
                    .enabled(false)
                    .visible(modSettings.version.orEmpty().isNotEmpty())
                //supportedVersion
                label(PlsBundle.message("mod.settings.supportedVersion")).widthGroup("mod.settings.right")
                textField()
                    .text(modSettings.supportedVersion.orEmpty())
                    .align(Align.FILL)
                    .columns(18)
                    .enabled(false)
                    .visible(modSettings.supportedVersion.orEmpty().isNotEmpty())
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
                //quickSelectGameDirectory
                link(PlsBundle.message("mod.settings.quickSelectGameDirectory")) { quickSelectGameDirectory() }
            }
            row {
                //gameDirectory
                label(PlsBundle.message("mod.settings.gameDirectory")).widthGroup("mod.settings.label")
                val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    .withTitle(PlsBundle.message("mod.settings.gameDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypePropertyKey, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .bindText(gameDirectoryProperty)
                    .align(Align.FILL)
                    .columns(36)
                    .validationOnApply { validateGameDirectory() }
            }
            row {
                //modPath
                label(PlsBundle.message("mod.settings.modPath")).widthGroup("mod.settings.label")
                val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    .withTitle(PlsBundle.message("mod.settings.modPath.title"))
                    .apply { putUserData(PlsDataKeys.gameTypePropertyKey, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .bindText(modPathProperty)
                    .align(Align.FILL)
                    .columns(36)
                    .enabled(false)
            }
            
            collapsibleGroup(PlsBundle.message("mod.settings.modDependencies"), false) {
                //TODO 0.8.1
            }
        }
    }
    
    private fun quickSelectGameDirectory() {
        val gameType = modSettings.gameType
        val targetPath = getSteamGamePath(gameType.gameSteamId, gameType.gameName) ?: return
        modSettings.gameDirectory = targetPath
    }
    
    private fun ValidationInfoBuilder.validateGameDirectory(): ValidationInfo? {
        //验证游戏目录是否合法
        //* 路径合法
        //* 路径对应的目录存在
        //* 路径
        val path = gameDirectory.toPathOrNull()
            ?: return error(PlsBundle.message("mod.settings.gameDirectory.error.1"))
        val rootFile = VfsUtil.findFile(path, false)?.takeIf { it.exists() }
            ?: return error(PlsBundle.message("mod.settings.gameDirectory.error.2"))
        val rootInfo = ParadoxCoreHandler.resolveRootInfo(rootFile, false)
        if(rootInfo?.rootType != ParadoxRootType.Game) {
            return error(PlsBundle.message("mod.settings.gameDirectory.error.3", gameType.description))
        }
        return null
    }
    
    override fun doOKAction() {
        super.doOKAction()
        
        project.messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onChange(project, modSettings)
    }
}