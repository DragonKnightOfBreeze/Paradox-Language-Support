package icu.windea.pls.core.tool

import com.intellij.openapi.application.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.listeners.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

class ParadoxGameSettingsDialog(
    val project: Project,
    val settings: ParadoxGameSettingsState
) : DialogWrapper(project, true) {
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(settings.gameType ?: getSettings().defaultGameType)
        .apply { afterChange { settings.gameType = it } }
    
    init {
        title = PlsBundle.message("game.settings")
        init()
    }
    
    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                //gameType
                label(PlsBundle.message("game.settings.gameType")).widthGroup("left")
                comboBox(ParadoxGameType.valueList)
                    .bindItem(gameTypeProperty)
                    .align(Align.FILL)
                    .columns(18)
                    .enabled(false)
                //gameVersion
                label(PlsBundle.message("game.settings.gameVersion")).widthGroup("right")
                textField()
                    .text(settings.gameVersion.orEmpty())
                    .align(Align.FILL)
                    .columns(18)
                    .enabled(false)
            }
            row {
                //gameDirectory
                label(PlsBundle.message("game.settings.gameDirectory")).widthGroup("left")
                val descriptor = ParadoxRootDirectoryDescriptor()
                    .withTitle(PlsBundle.message("game.settings.gameDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypePropertyKey, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .text(settings.gameDirectory.orEmpty())
                    .align(Align.FILL)
                    .columns(36)
                    .enabled(false)
            }
            
            //modDependencies
            collapsibleGroup(PlsBundle.message("game.settings.modDependencies"), false) {
                row {
                    cell(createModDependenciesPanel(project, settings)).align(Align.CENTER)
                }
            }
        }
    }
    
    override fun doOKAction() {
        getProfilesSettings().updateSettings()
        
        val messageBus = ApplicationManager.getApplication().messageBus
        messageBus.syncPublisher(ParadoxGameSettingsListener.TOPIC).onChange(settings)
        
        super.doOKAction()
    }
}