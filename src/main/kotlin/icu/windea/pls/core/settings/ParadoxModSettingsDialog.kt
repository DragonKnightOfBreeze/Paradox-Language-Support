package icu.windea.pls.core.settings

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.lang.model.*

class ParadoxModSettingsDialog(
    val project: Project,
    val modSettings: ParadoxModSettingsState,
): DialogWrapper(project, true) {
    //name (readonly)
    //version (readonly) supportedVersion? (readonly)
    //comment
    
    //game type (combobox)
    //game directory (filepath text field)
    //mod path (filepath text field)
    
    //mod dependencies (foldable group)
    //  mod dependencies table
    //  actions: add (select mod path & import from file), remove, move up, move down, edit
    //  columns: order (int text field), icon (thumbnail), name (readonly), version (readonly), supportedVersion (readonly)
    //  when add or edit a column: show edit dialog (+ mod path)
    
    override fun createCenterPanel(): DialogPanel {
        return panel {
            row {
                //name
                label(PlsBundle.message("mod.settings.name")).widthGroup("mod.settings.label")
                textField()
                    .text(modSettings.name.orEmpty())
                    .align(Align.FILL)
                    .columns(48)
                    .enabled(false)
            }
            row {
                //version
                label(PlsBundle.message("mod.settings.name")).widthGroup("mod.settings.label")
                textField()
                    .text(modSettings.version.orEmpty())
                    .align(Align.FILL)
                    .columns(48)
                    .enabled(false)
                    .visible(modSettings.version.orEmpty().isNotEmpty())
                //supportedVersion
                label(PlsBundle.message("mod.settings.name")).widthGroup("mod.settings.label")
                textField()
                    .text(modSettings.supportedVersion.orEmpty())
                    .align(Align.FILL)
                    .columns(48)
                    .enabled(false)
                    .visible(modSettings.supportedVersion.orEmpty().isNotEmpty())
            }
            row {
                comment(PlsBundle.message("mod.settings.comment"))
            }
            
            row {
                //gameType
                label(PlsBundle.message("mod.settings.gameType")).widthGroup("mod.settings.label")
                comboBox(ParadoxGameType.valueList)
                    .bindItem(modSettings::gameType.toNullableProperty())
                //quickSelectGameDirectory
                link(PlsBundle.message("mod.settings.quickSelectGameDirectory")) {
                    quickSelectGameDirectory()
                }.align(AlignX.RIGHT + AlignY.CENTER)
            }
            row {
                //gameDirectory
                label(PlsBundle.message("mod.settings.gameDirectory")).widthGroup("mod.settings.label")
                val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    .withTitle(PlsBundle.message("mod.settings.gameDirectory.title"))
                    .apply { putUserData(PlsDataKeys.gameTypeKey, modSettings::gameType) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .bindText(modSettings::gameDirectory.toNonNullableProperty(""))
                    .resizableColumn()
            }
            row {
                //modPath
                label(PlsBundle.message("mod.settings.modPath")).widthGroup("mod.settings.label")
                val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                    .withTitle(PlsBundle.message("mod.settings.modPath.title"))
                    .apply { putUserData(PlsDataKeys.gameTypeKey, modSettings::gameType) }
                textFieldWithBrowseButton(null, project, descriptor) { it.path }
                    .bindText(modSettings::modPath.toNonNullableProperty(""))
                    .resizableColumn()
                    .enabled(false)
            }
            
            collapsibleGroup(PlsBundle.message("mod.settings.modDependencies"), false) {
                
            }
        }
    }
    
    private fun quickSelectGameDirectory() {
        val gameType = modSettings.gameType
        val targetPath = getSteamGamePath(gameType.gameSteamId, gameType.gameName) ?: return
        modSettings.gameDirectory = targetPath
    }
}