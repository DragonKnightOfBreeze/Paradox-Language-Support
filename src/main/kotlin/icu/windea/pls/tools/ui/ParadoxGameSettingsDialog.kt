package icu.windea.pls.tools.ui

import com.intellij.openapi.application.*
import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*

@Suppress("UnstableApiUsage")
class ParadoxGameSettingsDialog(
    val project: Project,
    val settings: ParadoxGameSettingsState
) : DialogWrapper(project, true) {
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(settings.gameType ?: PlsFacade.getSettings().defaultGameType)

    val modDependencies = settings.copyModDependencies()

    init {
        title = PlsBundle.message("game.settings")
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                //gameType
                label(PlsBundle.message("game.settings.gameType")).widthGroup("left")
                comboBox(ParadoxGameType.entries)
                    .bindItem(gameTypeProperty)
                    .align(Align.FILL)
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
                //gameVersion
                label(PlsBundle.message("game.settings.gameVersion")).widthGroup("right")
                textField()
                    .text(settings.gameVersion.orEmpty())
                    .align(Align.FILL)
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
            }
            row {
                //gameDirectory
                label(PlsBundle.message("game.settings.gameDirectory")).widthGroup("left")
                val descriptor = FileChooserDescriptorFactory.singleDir()
                    .withTitle(PlsBundle.message("game.settings.gameDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(descriptor, project)
                    .text(settings.gameDirectory.orEmpty())
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

    override fun doOKAction() {
        super.doOKAction()
        doOk()
    }

    private fun doOk() {
        settings.modDependencies = modDependencies
        PlsFacade.getProfilesSettings().updateSettings()

        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxGameSettingsListener.TOPIC).onChange(settings)
    }

}
