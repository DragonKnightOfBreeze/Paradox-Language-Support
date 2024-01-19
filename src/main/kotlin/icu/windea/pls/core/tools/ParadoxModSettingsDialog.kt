package icu.windea.pls.core.tools

import com.intellij.openapi.application.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.asBrowseFolderDescriptor
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.panel
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.listeners.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

class ParadoxModSettingsDialog(
    val project: Project,
    val settings: ParadoxModSettingsState
) : DialogWrapper(project, true) {
    val oldGameType = settings.finalGameType
    
    val defaultGameVersion get() = ParadoxGameHandler.getGameVersionFromGameDirectory(defaultGameDirectory)
    val defaultGameDirectory get() = getSettings().defaultGameDirectories[oldGameType.id]
    
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(oldGameType)
    val gameVersionProperty = graph.property(settings.gameVersion.orEmpty())
    val gameDirectoryProperty = graph.property(settings.gameDirectory.orEmpty())
    
    init {
        gameVersionProperty.dependsOn(gameDirectoryProperty) { ParadoxGameHandler.getGameVersionFromGameDirectory(gameDirectory).orEmpty() }
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
                    .withTitle(PlsBundle.message("gameDirectory.title"))
                    .asBrowseFolderDescriptor()
                    .apply { putUserData(PlsDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .applyToComponent { defaultGameDirectory?.orNull()?.let { jbTextField.emptyText.setText(it) } }
                    .bindText(gameDirectoryProperty)
                    .columns(36)
                    .align(Align.FILL)
                    .validationOnApply { ParadoxGameHandler.validateGameDirectory(this, gameType, gameDirectory) }
            }
            val quickGameDirectory = ParadoxGameHandler.getQuickGameDirectory(gameType)
            row {
                link(PlsBundle.message("gameDirectory.quickSelect")) f@{
                    if(gameDirectory.isNotNullOrEmpty()) return@f
                    gameDirectory = quickGameDirectory ?: return@f
                }.enabled(quickGameDirectory != null)
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
    
    private fun handleModSettings() {
        //如果需要，加上缺失的模组自身的模组依赖配置
        if(modDependencies.find { it.modDirectory == settings.modDirectory } == null) {
            val newSettings = ParadoxModDependencySettingsState()
            newSettings.modDirectory = settings.modDirectory
            modDependencies.add(newSettings)
        }
    }
    
    override fun doOKAction() {
        super.doOKAction()
        doOk()
    }
    
    private fun doOk() {
        settings.gameType = gameType
        settings.gameDirectory = gameDirectory
        settings.modDependencies = modDependencies
        getProfilesSettings().updateSettings()
        
        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onChange(settings)
        
        if(oldGameType != settings.gameType) {
            messageBus.syncPublisher(ParadoxModGameTypeListener.TOPIC).onChange(settings)
        }
    }
}

