package icu.windea.pls.config.settings

import com.intellij.openapi.fileChooser.*
import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import icu.windea.pls.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*
import java.awt.event.*

@Suppress("UnstableApiUsage")
class PlsConfigSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.config")), SearchableConfigurable {
    override fun getId() = "pls.config"

    private val groupName = "pls.config"
    private val callbackLock = mutableSetOf<String>()

    override fun createPanel(): DialogPanel {
        callbackLock.clear()
        val settings = PlsFacade.getConfigSettings()
        return panel {
            lateinit var cbRemote: JBCheckBox
            lateinit var cbLocal: JBCheckBox
            lateinit var cbProjectLocal: JBCheckBox

            //enableBuiltInConfigGroups
            row {
                checkBox(PlsBundle.message("settings.config.enableBuiltInConfigGroups"))
                    .bindSelected(settings::enableBuiltInConfigGroups)
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged() }
                contextHelp(PlsBundle.message("settings.config.enableBuiltInConfigGroups.tip"))
            }
            //enableRemoteConfigGroups
            row {
                checkBox(PlsBundle.message("settings.config.enableRemoteConfigGroups"))
                    .bindSelected(settings::enableRemoteConfigGroups)
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged() }
                    .applyToComponent { cbRemote = this }
                contextHelp(PlsBundle.message("settings.config.enableRemoteConfigGroups.tip"))
                comment(PlsBundle.message("settings.config.remoteConfigDirectory.comment", MAX_LINE_LENGTH_WORD_WRAP))
            }
            //remoteConfigDirectory
            row {
                label(PlsBundle.message("settings.config.remoteConfigDirectory")).widthGroup(groupName)
                    .applyToComponent { toolTipText = PlsBundle.message("settings.config.remoteConfigDirectory.tip") }
                val descriptor = FileChooserDescriptorFactory.singleDir()
                    .withTitle(PlsBundle.message("settings.config.remoteConfigDirectory.title"))
                textFieldWithBrowseButton(descriptor, null)
                    .bindText(settings::remoteConfigDirectory.toNonNullableProperty(""))
                    .applyToComponent { setEmptyState(PlsBundle.message("not.configured")) }
                    .align(Align.FILL)
                    .onApply {
                        PlsConfigSettingsManager.onConfigDirectoriesChanged()
                        PlsConfigSettingsManager.onRemoteConfigDirectoriesChanged()
                    }
            }.enabledIf(cbRemote.selected)
            //configRepositoryUrls
            row {
                label(PlsBundle.message("settings.config.configRepositoryUrls")).widthGroup(groupName)
                    .applyToComponent { toolTipText = PlsBundle.message("settings.config.configRepositoryUrls.tip") }
                val configRepositoryUrls = settings.configRepositoryUrls
                ParadoxGameType.entries.forEach { configRepositoryUrls.putIfAbsent(it.id, PlsConfigRepositoryManager.getDefaultUrl(it)) }
                val defaultList = configRepositoryUrls.toMutableEntryList()
                var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                val action = { _: ActionEvent ->
                    val dialog = ConfigRepositoryUrlsDialog(list)
                    if (dialog.showAndGet()) list = dialog.resultList
                }
                link(PlsBundle.message("settings.config.configRepositoryUrls.link"), action)
                    .onApply {
                        val oldConfigRepositoryUrls = configRepositoryUrls.toMutableMap()
                        val newConfigRepositoryUrls = list.toMutableMap()
                        if (oldConfigRepositoryUrls == newConfigRepositoryUrls) return@onApply
                        settings.configRepositoryUrls = newConfigRepositoryUrls
                        PlsConfigSettingsManager.onRemoteConfigDirectoriesChanged()
                    }
                    .onReset { list = defaultList }
                    .onIsModified { list != defaultList }
            }.enabledIf(cbRemote.selected)
            //enableLocalConfigGroups
            row {
                checkBox(PlsBundle.message("settings.config.enableLocalConfigGroups"))
                    .bindSelected(settings::enableLocalConfigGroups)
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged() }
                    .applyToComponent { cbLocal = this }
                contextHelp(PlsBundle.message("settings.config.enableLocalConfigGroups.tip"))
            }
            //localConfigDirectory
            row {
                label(PlsBundle.message("settings.config.localConfigDirectory")).widthGroup(groupName)
                    .applyToComponent { toolTipText = PlsBundle.message("settings.config.localConfigDirectory.tip") }
                val descriptor = FileChooserDescriptorFactory.singleDir()
                    .withTitle(PlsBundle.message("settings.config.localConfigDirectory.title"))
                textFieldWithBrowseButton(descriptor, null)
                    .bindText(settings::localConfigDirectory.toNonNullableProperty(""))
                    .applyToComponent { setEmptyState(PlsBundle.message("not.configured")) }
                    .align(Align.FILL)
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged() }
            }.enabledIf(cbLocal.selected)
            //enableProjectLocalConfigGroups
            row {
                checkBox(PlsBundle.message("settings.config.enableProjectLocalConfigGroups"))
                    .bindSelected(settings::enableProjectLocalConfigGroups)
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged() }
                    .applyToComponent { cbProjectLocal = this }
                contextHelp(PlsBundle.message("settings.config.enableProjectLocalConfigGroups.tip"))
            }
            //projectLocalConfigDirectoryName
            row {
                label(PlsBundle.message("settings.config.projectLocalConfigDirectoryName")).widthGroup(groupName)
                    .applyToComponent { toolTipText = PlsBundle.message("settings.config.projectLocalConfigDirectoryName.tip") }
                textField()
                    .bindText(settings::projectLocalConfigDirectoryName.toNonNullableProperty(""))
                    .applyToComponent { setEmptyState(".config") }
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged() }
            }.enabledIf(cbProjectLocal.selected)
        }
    }
}
