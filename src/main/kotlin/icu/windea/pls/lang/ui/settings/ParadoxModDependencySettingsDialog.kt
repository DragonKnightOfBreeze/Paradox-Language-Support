package icu.windea.pls.lang.ui.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.listCellRenderer.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.actions.ChronicleDataKeys
import icu.windea.pls.lang.settings.ParadoxModDependencySettingsState
import icu.windea.pls.model.ParadoxGameType
import java.awt.Component

@Suppress("UnstableApiUsage")
class ParadoxModDependencySettingsDialog(
    val project: Project,
    val settings: ParadoxModDependencySettingsState,
    parentComponent: Component? = null
) : DialogWrapper(project, parentComponent, true, IdeModalityType.IDE) {
    val graph = PropertyGraph()
    val gameTypeProperty = graph.property(settings.finalGameType)

    init {
        title = ChronicleBundle.message("mod.dependency.settings")
        init()
    }

    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                // name
                label(ChronicleBundle.message("mod.dependency.settings.name")).widthGroup("left")
                textField()
                    .text(settings.name.orEmpty())
                    .columns(COLUMNS_LARGE)
                    .align(Align.FILL)
                    .enabled(false)
            }
            row {
                // version
                label(ChronicleBundle.message("mod.dependency.settings.version")).widthGroup("left")
                textField()
                    .text(settings.version.orEmpty())
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
                // supportedVersion
                label(ChronicleBundle.message("mod.dependency.settings.supportedVersion")).widthGroup("right")
                textField()
                    .text(settings.supportedVersion.orEmpty())
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
                    .visible(settings.supportedVersion.orEmpty().isNotEmpty())
            }
            row {
                // gameType
                label(ChronicleBundle.message("mod.dependency.settings.gameType")).widthGroup("left")
                comboBox(ParadoxGameType.getAllSpecific(), textListCellRenderer { it?.title })
                    .bindItem(gameTypeProperty)
                    .columns(COLUMNS_SHORT)
                    .enabled(false)
            }
            row {
                // modDirectory
                label(ChronicleBundle.message("mod.dependency.settings.modDirectory")).widthGroup("left")
                val descriptor = FileChooserDescriptorFactory.singleDir()
                    .withTitle(ChronicleBundle.message("modDirectory.title"))
                    .apply { putUserData(ChronicleDataKeys.gameTypeProperty, gameTypeProperty) }
                textFieldWithBrowseButton(descriptor, project)
                    .text(settings.modDirectory.orEmpty())
                    .columns(COLUMNS_LARGE)
                    .align(Align.FILL)
                    .enabled(false)
            }
        }
    }

    override fun getDimensionServiceKey() = "Chronicle.ParadoxModDependencySettingsDialog" // 持久化对话框的位置
}
