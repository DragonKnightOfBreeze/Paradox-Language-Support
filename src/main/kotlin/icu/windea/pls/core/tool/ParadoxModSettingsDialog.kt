package icu.windea.pls.core.tool

import com.intellij.openapi.application.*
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
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*

class ParadoxModSettingsDialog(
    val project: Project,
    val settings: ParadoxModSettingsState
) : DialogWrapper(project, true) {
    val oldGameType = settings.gameType ?: getSettings().defaultGameType
    
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(settings.gameType ?: getSettings().defaultGameType)
        .apply { afterChange { settings.gameType = it } }
    val gameDirectoryProperty = graph.property(settings.gameDirectory.orEmpty())
        .apply { afterChange { settings.gameDirectory = it } }
    
    var gameType by gameTypeProperty
    var gameDirectory by gameDirectoryProperty
    
    init {
        title = PlsBundle.message("mod.settings")
        handleModSettings()
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
                    .text(settings.name.orEmpty())
                    .align(Align.FILL)
                    .columns(36)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.settings.version")).widthGroup("mod.settings.left")
                textField()
                    .text(settings.version.orEmpty())
                    .align(Align.FILL)
                    .columns(18)
                    .enabled(false)
                    .visible(settings.version.orEmpty().isNotEmpty())
                //supportedVersion
                label(PlsBundle.message("mod.settings.supportedVersion")).widthGroup("mod.settings.right")
                textField()
                    .text(settings.supportedVersion.orEmpty())
                    .align(Align.FILL)
                    .columns(18)
                    .enabled(false)
                    .visible(settings.supportedVersion.orEmpty().isNotEmpty())
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
                    cell(createModDependenciesPanel(project, settings, settings.modDependencies)).align(Align.CENTER)
                }
            }
        }
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
        getProfilesSettings().updateSettings()
        
        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onChange(settings)
        if(oldGameType != settings.gameType) {
            messageBus.syncPublisher(ParadoxModGameTypeListener.TOPIC).onChange(settings)
        }
    }
}