package icu.windea.pls.config.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.setEmptyState
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.config.util.CwtConfigRepositoryManager
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.core.util.toMutableEntryList
import icu.windea.pls.core.util.toMutableMap
import icu.windea.pls.ide.help.PlsHelpTopics
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsConstants
import java.awt.event.ActionEvent

@Suppress("UnstableApiUsage")
class PlsConfigSettingsConfigurable : BoundConfigurable(ChronicleBundle.message("settings.config")), SearchableConfigurable {
    private val callbackLock = CallbackLock()

    override fun getId() = "pls.config"

    override fun getHelpTopic() = PlsHelpTopics.configSettings

    override fun createPanel(): DialogPanel {
        callbackLock.reset()
        return panel {
            group(ChronicleBundle.message("settings.config.configGroups")) { configureGroupForConfigGroups() }
            // features
            collapsibleGroup(ChronicleBundle.message("settings.config.features")) { configureGroupForFeatures() }
        }
    }

    private fun Panel.configureGroupForConfigGroups() {
        val groupName = "pls.config.configGroups"
        val settings = PlsConfigSettings.getInstance().state

        row {
            comment(ChronicleBundle.message("settings.config.configGroups.comment", MAX_LINE_LENGTH_WORD_WRAP))
        }
        // enableBuiltInConfigGroups
        row {
            checkBox(ChronicleBundle.message("settings.config.enableBuiltInConfigGroups"))
                .comment(ChronicleBundle.message("settings.config.enableBuiltInConfigGroups.comment", MAX_LINE_LENGTH_WORD_WRAP))
                .bindSelected(settings::enableBuiltInConfigGroups)
                .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
            browserLink(ChronicleBundle.message("link.documentation"), PlsConstants.docUrl("config.html#config-group-builtin"))
        }
        // enableRemoteConfigGroups
        row {
            checkBox(ChronicleBundle.message("settings.config.enableRemoteConfigGroups"))
                .comment(ChronicleBundle.message("settings.config.enableRemoteConfigGroups.comment", MAX_LINE_LENGTH_WORD_WRAP))
                .bindSelected(settings::enableRemoteConfigGroups)
                .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
            browserLink(ChronicleBundle.message("link.documentation"), PlsConstants.docUrl("config.html#config-group-remote"))
        }
        // enableLocalConfigGroups
        row {
            checkBox(ChronicleBundle.message("settings.config.enableLocalConfigGroups"))
                .comment(ChronicleBundle.message("settings.config.enableLocalConfigGroups.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                .bindSelected(settings::enableLocalConfigGroups)
                .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
            browserLink(ChronicleBundle.message("link.documentation"), PlsConstants.docUrl("config.html#config-group-local"))
        }
        // enableProjectLocalConfigGroups
        row {
            checkBox(ChronicleBundle.message("settings.config.enableProjectLocalConfigGroups"))
                .comment(ChronicleBundle.message("settings.config.enableProjectLocalConfigGroups.comment"), MAX_LINE_LENGTH_WORD_WRAP)
                .bindSelected(settings::enableProjectLocalConfigGroups)
                .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
            browserLink(ChronicleBundle.message("link.documentation"), PlsConstants.docUrl("config.html#config-group-project-local"))
        }

        // remoteConfigDirectory
        row {
            label(ChronicleBundle.message("settings.config.remoteConfigDirectory")).widthGroup(groupName)
                .comment(ChronicleBundle.message("settings.config.remoteConfigDirectory.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            val descriptor = FileChooserDescriptorFactory.singleDir()
                .withTitle(ChronicleBundle.message("settings.config.remoteConfigDirectory.title"))
            textFieldWithBrowseButton(descriptor, null)
                .bindText(settings::remoteConfigDirectory.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(ChronicleBundle.message("not.configured")) }
                .align(Align.FILL)
                .onApply {
                    PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock)
                    PlsConfigSettingsManager.onRemoteConfigDirectoriesChanged(callbackLock)
                }
        }
        // configRepositoryUrls
        row {
            label(ChronicleBundle.message("settings.config.configRepositoryUrls")).widthGroup(groupName)
                .comment(ChronicleBundle.message("settings.config.configRepositoryUrls.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            val configRepositoryUrls = settings.configRepositoryUrls
            ParadoxGameType.getAllSpecific().forEach { configRepositoryUrls.putIfAbsent(it.id, CwtConfigRepositoryManager.getDefaultUrl(it)) }
            val defaultList = configRepositoryUrls.toMutableEntryList()
            var list = defaultList.mapTo(mutableListOf()) { it.copy() }
            val action = { _: ActionEvent ->
                val dialog = ConfigRepositoryUrlsDialog(list)
                if (dialog.showAndGet()) list = dialog.resultList
            }
            link(ChronicleBundle.message("link.configure"), action)
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
            label(ChronicleBundle.message("settings.config.localConfigDirectory")).widthGroup(groupName)
                .comment(ChronicleBundle.message("settings.config.localConfigDirectory.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            val descriptor = FileChooserDescriptorFactory.singleDir()
                .withTitle(ChronicleBundle.message("settings.config.localConfigDirectory.title"))
            textFieldWithBrowseButton(descriptor, null)
                .bindText(settings::localConfigDirectory.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(ChronicleBundle.message("not.configured")) }
                .align(Align.FILL)
                .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
        }
        // projectLocalConfigDirectoryName
        row {
            label(ChronicleBundle.message("settings.config.projectLocalConfigDirectoryName")).widthGroup(groupName)
                .comment(ChronicleBundle.message("settings.config.projectLocalConfigDirectoryName.comment"), MAX_LINE_LENGTH_WORD_WRAP)
            textField()
                .bindText(settings::projectLocalConfigDirectoryName.toNonNullableProperty(""))
                .applyToComponent { setEmptyState(".config") }
                .onApply { PlsConfigSettingsManager.onConfigDirectoriesChanged(callbackLock) }
        }
        // overrideBuiltIn
        row {
            checkBox(ChronicleBundle.message("settings.config.overrideBuiltIn"))
                .bindSelected(settings::overrideBuiltIn)
                .onApply { PlsConfigSettingsManager.onRemoteConfigDirectoriesChanged(callbackLock) }
        }
    }

    private fun Panel.configureGroupForFeatures() {
        // val group = "pls.config.features"
        val settings = PlsConfigSettings.getInstance().state.features

        // checkComparisonOperators
        row {
            checkBox(ChronicleBundle.message("settings.config.features.checkComparisonOperators"))
                .bindSelected(settings::checkComparisonOperators)
            contextHelp(ChronicleBundle.message("settings.config.features.checkComparisonOperators.tip"))
        }
    }
}
