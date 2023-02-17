package icu.windea.pls.core.tool

import com.intellij.openapi.observable.properties.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.settings.*
import icu.windea.pls.lang.model.*

class ParadoxModDependencySettingsDialog(
    val project: Project,
    val settings: ParadoxModDependencySettingsState
) : DialogWrapper(project, true) {
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(settings.gameType ?: getSettings().defaultGameType)
        .apply { afterChange { settings.gameType = it } }
    
    init {
        title = PlsBundle.message("mod.dependency.settings")
        init()
    }
    
    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                //name
                label(PlsBundle.message("mod.dependency.settings.name")).widthGroup("mod.dependency.settings.left")
                textField()
                    .text(settings.name.orEmpty())
                    .align(Align.FILL)
                    .columns(32)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.dependency.settings.version")).widthGroup("mod.dependency.settings.left")
                textField()
                    .text(settings.version.orEmpty())
                    .align(Align.FILL)
                    .columns(16)
                    .enabled(false)
                    .visible(settings.version.orEmpty().isNotEmpty())
                //supportedVersion
                label(PlsBundle.message("mod.dependency.settings.supportedVersion")).widthGroup("mod.dependency.settings.right")
                textField()
                    .text(settings.supportedVersion.orEmpty())
                    .align(Align.FILL)
                    .columns(16)
                    .enabled(false)
                    .visible(settings.supportedVersion.orEmpty().isNotEmpty())
            }
            row {
                comment(PlsBundle.message("mod.dependency.settings.comment"))
            }
            row {
                //gameType
                label(PlsBundle.message("mod.dependency.settings.gameType")).widthGroup("mod.dependency.settings.left")
                comboBox(ParadoxGameType.valueList)
                    .align(Align.FILL)
                    .columns(16)
                    .enabled(false)
            }
            row {
                //modDirectory
                label(PlsBundle.message("mod.dependency.settings.modDirectory")).widthGroup("mod.dependency.settings.left")
                val descriptor = ParadoxRootDirectoryDescriptor()
                    .withTitle(PlsBundle.message("mod.dependency.settings.modDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypePropertyKey, gameTypeProperty) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .text(settings.modDirectory.orEmpty())
                    .align(Align.FILL)
                    .columns(32)
                    .enabled(false)
            }
        }
    }
    
    //override fun createDefaultActions() {
    //    okAction.isEnabled = false
    //    cancelAction.putValue(Action.NAME, IdeBundle.message("action.close"))
    //}
}