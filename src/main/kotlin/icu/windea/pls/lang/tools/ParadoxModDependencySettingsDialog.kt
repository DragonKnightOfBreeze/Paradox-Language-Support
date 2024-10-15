package icu.windea.pls.lang.tools

import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.BrowseFolderDescriptor.Companion.asBrowseFolderDescriptor
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.lang.settings.*
import icu.windea.pls.model.ParadoxGameType.*
import java.awt.*

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
                    .columns(36)
                    .align(Align.FILL)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.dependency.settings.version")).widthGroup("left")
                textField()
                    .text(settings.version.orEmpty())
                    .columns(18)
                    .enabled(false)
                //supportedVersion
                label(PlsBundle.message("mod.dependency.settings.supportedVersion")).widthGroup("right")
                textField()
                    .text(settings.supportedVersion.orEmpty())
                    .columns(18)
                    .enabled(false)
                    .visible(settings.supportedVersion.orEmpty().isNotEmpty())
            }
            row {
                //gameType
                label(PlsBundle.message("mod.dependency.settings.gameType")).widthGroup("left")
                comboBox(entries)
                    .bindItem(gameTypeProperty)
                    .columns(18)
                    .enabled(false)
            }
            row {
                //modDirectory
                label(PlsBundle.message("mod.dependency.settings.modDirectory")).widthGroup("left")
                val descriptor = ParadoxDirectoryDescriptor()
                    .withTitle(PlsBundle.message("mod.dependency.settings.modDirectory.title"))
                    .asBrowseFolderDescriptor()
                    .apply { putUserData(PlsDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
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
