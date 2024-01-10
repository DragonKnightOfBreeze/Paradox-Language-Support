package icu.windea.pls.core.tools

import com.intellij.openapi.application.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.asBrowseFolderDescriptor
import com.intellij.openapi.vfs.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.listeners.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.model.*

class ParadoxModSettingsDialog(
    val project: Project,
    val settings: ParadoxModSettingsState
) : DialogWrapper(project, true) {
    val oldGameType = settings.finalGameType
    
    val defaultGameVersion get() = getGameVersionFromGameDirectory(defaultGameDirectory)
    val defaultGameDirectory get() = getSettings().defaultGameDirectories[oldGameType.id]
    
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(oldGameType)
    val gameVersionProperty = graph.property(settings.gameVersion.orEmpty())
    val gameDirectoryProperty = graph.property(settings.gameDirectory.orEmpty())
    
    init {
        gameVersionProperty.dependsOn(gameDirectoryProperty) { getGameVersionFromGameDirectory(gameDirectory).orEmpty() }
    }
    
    var gameType by gameTypeProperty
    var gameVersion by gameVersionProperty
    var gameDirectory by gameDirectoryProperty
    val modDependencies = settings.copyModDependencies()
    
    init {
        title = PlsBundle.message("mod.settings")
        handleModSettings()
        init()
    }
    
    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                //name
                label(PlsBundle.message("mod.settings.name")).widthGroup("left")
                textField()
                    .text(settings.name.orEmpty())
                    .columns(36)
                    .align(Align.FILL)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.settings.version")).widthGroup("left")
                textField()
                    .text(settings.version.orEmpty())
                    .columns(18)
                    .enabled(false)
                //supportedVersion
                label(PlsBundle.message("mod.settings.supportedVersion")).widthGroup("right")
                textField()
                    .text(settings.supportedVersion.orEmpty())
                    .columns(18)
                    .enabled(false)
                    .visible(settings.supportedVersion.orEmpty().isNotEmpty())
            }
            row {
                //gameType
                label(PlsBundle.message("mod.settings.gameType")).widthGroup("left")
                comboBox(ParadoxGameType.valueList)
                    .bindItem(gameTypeProperty)
                    .columns(18)
                    .onApply { settings.gameType = gameTypeProperty.get() } //set game type to non-default on apply
                    .enabled(settings.inferredGameType == null) //disabled if game type can be inferred
                //gameVersion
                label(PlsBundle.message("mod.settings.gameVersion")).widthGroup("right")
                textField()
                    .applyToComponent { defaultGameVersion?.orNull()?.let { emptyText.setText(it) } }
                    .bindText(gameVersionProperty)
                    .columns(18)
                    .enabled(false)
            }
            row {
                //gameDirectory
                label(PlsBundle.message("mod.settings.gameDirectory")).widthGroup("left")
                val descriptor = ParadoxDirectoryDescriptor()
                    .withTitle(PlsBundle.message("mod.settings.gameDirectory.title"))
                    .asBrowseFolderDescriptor()
                    .apply { putUserData(PlsDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .applyToComponent { defaultGameDirectory?.orNull()?.let { jbTextField.emptyText.setText(it) } }
                    .bindText(gameDirectoryProperty)
                    .columns(36)
                    .align(Align.FILL)
                    .validationOnApply { validateGameDirectory() }
            }
            row {
                //quickSelectGameDirectory
                link(PlsBundle.message("mod.settings.quickSelectGameDirectory")) { gameDirectory = quickSelectGameDirectory().orEmpty() }
                    visible(getSteamGamePath(gameType.steamId, gameType.title) != null)
            }
            row {
                //modDirectory
                label(PlsBundle.message("mod.settings.modDirectory")).widthGroup("left")
                val descriptor = ParadoxDirectoryDescriptor()
                    .withTitle(PlsBundle.message("mod.settings.modDirectory.title"))
                    .asBrowseFolderDescriptor()
                    .apply { putUserData(PlsDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .text(settings.modDirectory.orEmpty())
                    .columns(36)
                    .align(Align.FILL)
                    .enabled(false)
            }
            
            //modDependencies
            collapsibleGroup(PlsBundle.message("mod.settings.modDependencies"), false) {
                row {
                    cell(ParadoxModDependenciesTableModel.createPanel(project, settings, modDependencies))
                        .align(Align.FILL)
                }.resizableRow()
                row {
                    comment(PlsBundle.message("mod.dependencies.comment.1"))
                }
            }.resizableRow()
        }
    }
    
    private fun ValidationInfoBuilder.validateGameDirectory(): ValidationInfo? {
        //验证游戏目录是否合法
        //* 路径合法
        //* 路径对应的目录存在
        //* 路径是游戏目录（可以查找到对应的launcher-settings.json）
        val gameDirectory = gameDirectory.normalizeAbsolutePath()
        if(gameDirectory.isEmpty()) return null //为空时跳过检查
        val path = gameDirectory.toPathOrNull()
        if(path == null) return error(PlsBundle.message("mod.settings.gameDirectory.error.1"))
        val rootFile = VfsUtil.findFile(path, false)?.takeIf { it.exists() }
        if(rootFile == null) return error(PlsBundle.message("mod.settings.gameDirectory.error.2"))
        val rootInfo = rootFile.rootInfo
        if(rootInfo !is ParadoxGameRootInfo) return error(PlsBundle.message("mod.settings.gameDirectory.error.3", gameType.title))
        return null
    }
    
    private fun quickSelectGameDirectory(): String? {
        return getSteamGamePath(gameType.steamId, gameType.title)
    }
    
    private fun getGameVersionFromGameDirectory(gameDirectory: String?): String? {
        val gameDirectory0 = gameDirectory?.orNull() ?: return null
        val rootFile = gameDirectory0.toVirtualFile(false)?.takeIf { it.exists() } ?: return null
        val rootInfo = rootFile.rootInfo
        if(rootInfo !is ParadoxGameRootInfo) return null
        return rootInfo.launcherSettingsInfo.rawVersion
    }
    
    private fun handleModSettings() {
        //如果需要，加上缺失的模组自身的模组依赖配置
        if(modDependencies.find { it.modDirectory == settings.modDirectory } == null) {
            val newSettings = ParadoxModDependencySettingsState()
            newSettings.modDirectory = settings.modDirectory
            modDependencies.add(newSettings)
        }
    }
    
    override fun doOKAction() {
        doOk()
        super.doOKAction()
    }
    
    private fun doApply() {
        settings.gameType = gameType
        settings.gameDirectory = gameDirectory
        settings.modDependencies = modDependencies
        getProfilesSettings().updateSettings()
    }
    
    private fun doOk() {
        doApply()
        
        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onChange(settings)
        
        if(oldGameType != settings.gameType) {
            messageBus.syncPublisher(ParadoxModGameTypeListener.TOPIC).onChange(settings)
        }
    }
}

