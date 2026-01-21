package icu.windea.pls.config.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import icu.windea.pls.model.constants.PlsConstants

/**
 * PLS 规则设置。可以在插件的对应设置页面中进行配置。
 */
@Service
@State(name = "PlsConfigSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsConfigSettings : SimplePersistentStateComponent<PlsConfigSettings.State>(State()) {
    companion object {
        @JvmStatic
        fun getInstance(): PlsConfigSettings = service()
    }

    /**
     * @property enableBuiltInConfigGroups 是否启用内置规则分组（共享的内置规则分组总是会被启用）。
     * @property enableRemoteConfigGroups 是否启用远程规则分组（不建议与内置规则分组同时启用）。
     * @property enableLocalConfigGroups 是否启用全局的本地规则分组。
     * @property enableProjectLocalConfigGroups 是否启用项目的本地规则分组。
     * @property remoteConfigDirectory 远程规则分组所在的根目录。
     * @property configRepositoryUrls 远程规则分组的仓库地址。
     * @property localConfigDirectory 全局的本地规则分组所在的根目录。
     * @property projectLocalConfigDirectoryName 项目的本地规则分组所在的根目录的名字。
     * @property overrideBuiltIn 如果远程仓库地址已配置，覆盖相关的内置规则分组。
     */
    class State : BaseState() {
        var enableBuiltInConfigGroups by property(true)
        var enableRemoteConfigGroups by property(false)
        var enableLocalConfigGroups by property(true)
        var enableProjectLocalConfigGroups by property(true)
        var remoteConfigDirectory by string()
        var configRepositoryUrls by map<String, String>()
        var localConfigDirectory by string()
        var projectLocalConfigDirectoryName by string(".config")
        var overrideBuiltIn by property(false)
    }
}
