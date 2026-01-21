package icu.windea.pls.config.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.util.CwtConfigRepositoryManager
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.core.util.toMutableEntryList
import icu.windea.pls.core.util.toMutableMap
import icu.windea.pls.model.ParadoxGameType
import java.awt.event.ActionEvent

@Suppress("UnstableApiUsage")
class PlsConfigSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.config")), SearchableConfigurable {
    override fun getId() = "pls.config"

    override fun getHelpTopic() = "icu.windea.pls.config.settings"

    private val groupName = "pls.config"
    private val callbackLock = CallbackLock()

    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        val settings = PlsConfigSettings.getInstance().state
        return panel {
            group(PlsBundle.message("settings.config.configGroups")) {

            }

            // enableBuiltInConfigGroups
            row {
                checkBox(PlsBundle.message("settings.config.enableBuiltInConfigGroups"))
                    .bindSelected(settings::enableBuiltInConfigGroups)
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
                contextHelp(PlsBundle.message("settings.config.enableBuiltInConfigGroups.tip"))
                comment(PlsBundle.message("settings.config.enableBuiltInConfigGroups.comment", MAX_LINE_LENGTH_WORD_WRAP))
            }
            // enableRemoteConfigGroups
            row {
                checkBox(PlsBundle.message("settings.config.enableRemoteConfigGroups"))
                    .bindSelected(settings::enableRemoteConfigGroups)
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
                contextHelp(PlsBundle.message("settings.config.enableRemoteConfigGroups.tip"))
                comment(PlsBundle.message("settings.config.enableRemoteConfigGroups.comment", MAX_LINE_LENGTH_WORD_WRAP))
            }
            // enableLocalConfigGroups
            row {
                checkBox(PlsBundle.message("settings.config.enableLocalConfigGroups"))
                    .bindSelected(settings::enableLocalConfigGroups)
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
                contextHelp(PlsBundle.message("settings.config.enableLocalConfigGroups.tip"))
            }
            // enableProjectLocalConfigGroups
            row {
                checkBox(PlsBundle.message("settings.config.enableProjectLocalConfigGroups"))
                    .bindSelected(settings::enableProjectLocalConfigGroups)
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
                contextHelp(PlsBundle.message("settings.config.enableProjectLocalConfigGroups.tip"))
            }

            // remoteConfigDirectory
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
                        PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock)
                        PlsConfigSettingsManager.onRemoteConfigDirectoriesChanged(callbackLock)
                    }
            }
            // configRepositoryUrls
            row {
                label(PlsBundle.message("settings.config.configRepositoryUrls")).widthGroup(groupName)
                    .applyToComponent { toolTipText = PlsBundle.message("settings.config.configRepositoryUrls.tip") }
                val configRepositoryUrls = settings.configRepositoryUrls
                ParadoxGameType.getAll().forEach { configRepositoryUrls.putIfAbsent(it.id, CwtConfigRepositoryManager.getDefaultUrl(it)) }
                val defaultList = configRepositoryUrls.toMutableEntryList()
                var list = defaultList.mapTo(mutableListOf()) { it.copy() }
                val action = { _: ActionEvent ->
                    val dialog = ConfigRepositoryUrlsDialog(list)
                    if (dialog.showAndGet()) list = dialog.resultList
                }
                link(PlsBundle.message("configure"), action)
                    .onApply {
                        val oldConfigRepositoryUrls = configRepositoryUrls.toMutableMap()
                        val newConfigRepositoryUrls = list.toMutableMap()
                        if (oldConfigRepositoryUrls == newConfigRepositoryUrls) return@onApply
                        settings.configRepositoryUrls = newConfigRepositoryUrls
                        PlsConfigSettingsManager.onRemoteConfigDirectoriesChanged(callbackLock)
                    }
                    .onReset { list = defaultList }
                    .onIsModified { list != defaultList }
            }
            // localConfigDirectory
            row {
                label(PlsBundle.message("settings.config.localConfigDirectory")).widthGroup(groupName)
                    .applyToComponent { toolTipText = PlsBundle.message("settings.config.localConfigDirectory.tip") }
                val descriptor = FileChooserDescriptorFactory.singleDir()
                    .withTitle(PlsBundle.message("settings.config.localConfigDirectory.title"))
                textFieldWithBrowseButton(descriptor, null)
                    .bindText(settings::localConfigDirectory.toNonNullableProperty(""))
                    .applyToComponent { setEmptyState(PlsBundle.message("not.configured")) }
                    .align(Align.FILL)
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
            }
            // projectLocalConfigDirectoryName
            row {
                label(PlsBundle.message("settings.config.projectLocalConfigDirectoryName")).widthGroup(groupName)
                    .applyToComponent { toolTipText = PlsBundle.message("settings.config.projectLocalConfigDirectoryName.tip") }
                textField()
                    .bindText(settings::projectLocalConfigDirectoryName.toNonNullableProperty(""))
                    .applyToComponent { setEmptyState(".config") }
                    .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
            }
            // overrideBuiltIn
            row {
                checkBox(PlsBundle.message("settings.config.overrideBuiltIn"))
                    .bindSelected(settings::overrideBuiltIn)
                    .onApply { PlsConfigSettingsManager.onRemoteConfigDirectoriesChanged(callbackLock) }
            }
        }
    }
}
