package icu.windea.pls.tools.ui

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.*
import java.awt.*

@Suppress("UnstableApiUsage")
class ParadoxModDependencySettingsDialog(
    val project: Project,
    val settings: ParadoxModDependencySettingsState,
    parentComponent: Component? = null
) : DialogWrapper(project, parentComponent, true, IdeModalityType.IDE) {
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(settings.finalGameType)

    init {
        title = PlsBundle.message("mod.dependency.settings")
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                //name
                label(PlsBundle.message("mod.dependency.settings.name")).widthGroup("left")
                textField()
                    .text(settings.name.orEmpty())
                    .columns(COLUMNS_LARGE)
                    .align(Align.FILL)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.dependency.settings.version")).widthGroup("left")
                textField()
                    .text(settings.version.orEmpty())
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
                //supportedVersion
                label(PlsBundle.message("mod.dependency.settings.supportedVersion")).widthGroup("right")
                textField()
                    .text(settings.supportedVersion.orEmpty())
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
                    .visible(settings.supportedVersion.orEmpty().isNotEmpty())
            }
            row {
                //gameType
                label(PlsBundle.message("mod.dependency.settings.gameType")).widthGroup("left")
                comboBox(ParadoxGameType.entries)
                    .bindItem(gameTypeProperty)
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
            }
            row {
                //modDirectory
                label(PlsBundle.message("mod.dependency.settings.modDirectory")).widthGroup("left")
                val descriptor = FileChooserDescriptorFactory.singleDir()
                    .withTitle(PlsBundle.message("mod.dependency.settings.modDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(descriptor, project)
                    .text(settings.modDirectory.orEmpty())
                    .columns(36)
                    .align(Align.FILL)
                    .enabled(false)
            }
        }
    }

    //override fun createDefaultActions() {
    //    okAction.isEnabled = false
    //    cancelAction.putValue(Action.NAME, IdeBundle.message("action.close"))
    //}
}
