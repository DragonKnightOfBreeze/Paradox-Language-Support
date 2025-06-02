package icu.windea.pls.config.settings

import com.intellij.openapi.components.*
import icu.windea.pls.*

/**
 * PLS规则设置。可以在插件的对应设置页面中进行配置。
 */
@Service(Service.Level.APP)
@State(name = "PlsConfigSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsConfigSettings : SimplePersistentStateComponent<PlsConfigSettings.State>(State()) {
    /**
     * @property enableBuiltInConfigGroups 是否启用内置的规则分组。
     * @property enableRemoteConfigGroups 是否启用远程的规则分组（不建议与内置的规则分组同时启用）。
     * @property remoteConfigDirectory 远程规则分组所在的根目录。
     * @property configRepositoryUrls 远程规则分组的仓库地址。
     * @property enableLocalConfigGroups 是否启用全局的本地规则分组。
     * @property localConfigDirectory 全局的本地规则分组所在的根目录。
     * @property enableProjectLocalConfigGroups 是否启用项目的本地规则分组。
     * @property projectLocalConfigDirectoryName 项目的本地规则分组所在的根目录的名字。
     */
    class State : BaseState() {
        var enableBuiltInConfigGroups by property(true)
        var enableRemoteConfigGroups by property(false)
        var remoteConfigDirectory by string()
        var configRepositoryUrls by map<String, String>()
        var enableLocalConfigGroups by property(true)
        var localConfigDirectory by string()
        var enableProjectLocalConfigGroups by property(true)
        var projectLocalConfigDirectoryName by string(".config")

        fun updateSettings() = incrementModificationCount()
    }
}
