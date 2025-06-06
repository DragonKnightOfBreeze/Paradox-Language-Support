package icu.windea.pls.tools.ui

import com.intellij.openapi.application.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

@Suppress("UnstableApiUsage")
class ParadoxModSettingsDialog(
    val project: Project,
    val settings: ParadoxModSettingsState
) : DialogWrapper(project, true) {
    val oldGameType = settings.finalGameType

    val defaultGameVersion get() = ParadoxCoreManager.getGameVersionFromGameDirectory(defaultGameDirectory)
    val defaultGameDirectory get() = PlsFacade.getSettings().defaultGameDirectories[oldGameType.id]

    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(oldGameType)
    val gameVersionProperty = graph.property(settings.gameVersion.orEmpty())
    val gameDirectoryProperty = graph.property(settings.gameDirectory.orEmpty())

    init {
        gameVersionProperty.dependsOn(gameDirectoryProperty) { ParadoxCoreManager.getGameVersionFromGameDirectory(gameDirectory).orEmpty() }
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
                    .columns(COLUMNS_LARGE)
                    .align(Align.FILL)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.settings.version")).widthGroup("left")
                textField()
                    .text(settings.version.orEmpty())
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
                //supportedVersion
                label(PlsBundle.message("mod.settings.supportedVersion")).widthGroup("right")
                textField()
                    .text(settings.supportedVersion.orEmpty())
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
                    .visible(settings.supportedVersion.orEmpty().isNotEmpty())
            }
            row {
                //gameType
                label(PlsBundle.message("mod.settings.gameType")).widthGroup("left")
                comboBox(ParadoxGameType.entries)
                    .bindItem(gameTypeProperty)
                    .columns(COLUMNS_SHORT)
                    .onApply { settings.gameType = gameTypeProperty.get() } //set game type to non-default on apply
                    .enabled(settings.inferredGameType == null) //disabled if game type can be inferred
                //gameVersion
                label(PlsBundle.message("mod.settings.gameVersion")).widthGroup("right")
                textField()
                    .applyToComponent { defaultGameVersion?.orNull()?.let { emptyText.text = it } }
                    .bindText(gameVersionProperty)
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
            }
            row {
                //gameDirectory
                label(PlsBundle.message("mod.settings.gameDirectory")).widthGroup("left")
                val descriptor = FileChooserDescriptorFactory.singleDir()
                    .withTitle(PlsBundle.message("gameDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(descriptor, project)
                    .applyToComponent { defaultGameDirectory?.orNull()?.let { jbTextField.emptyText.text = it } }
                    .bindText(gameDirectoryProperty)
                    .columns(COLUMNS_LARGE)
                    .align(Align.FILL)
                    .validationOnApply { ParadoxCoreManager.validateGameDirectory(this, gameType, gameDirectory) }
            }
            row {
                link(PlsBundle.message("gameDirectory.quickSelect")) f@{
                    val quickGameDirectory = ParadoxCoreManager.getQuickGameDirectory(gameType)?.orNull() ?: return@f
                    gameDirectory = quickGameDirectory
                }
            }
            row {
                //modDirectory
                label(PlsBundle.message("mod.settings.modDirectory")).widthGroup("left")
                val descriptor = FileChooserDescriptorFactory.singleDir()
                    .withTitle(PlsBundle.message("mod.settings.modDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(descriptor, project)
                    .text(settings.modDirectory.orEmpty())
                    .columns(COLUMNS_LARGE)
                    .align(Align.FILL)
                    .enabled(false)
            }

            //modDependencies
            collapsibleGroup(PlsBundle.message("mod.dependencies"), false) {
                row {
                    cell(ParadoxModDependenciesTable.createPanel(project, settings, modDependencies)).align(Align.FILL)
                }
                row {
                    comment(PlsBundle.message("mod.dependencies.comment.1"))
                }
            }
        }
    }

    private fun handleModSettings() {
        //如果需要，加上缺失的模组自身的模组依赖配置
        if (modDependencies.find { it.modDirectory == settings.modDirectory } == null) {
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
        PlsFacade.getProfilesSettings().updateSettings()

        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxModSettingsListener.TOPIC).onChange(settings)

        if (oldGameType != settings.gameType) {
            messageBus.syncPublisher(ParadoxModGameTypeListener.TOPIC).onChange(settings)
        }
    }
}
