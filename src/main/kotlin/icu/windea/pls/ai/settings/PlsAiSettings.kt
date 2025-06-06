package icu.windea.pls.ai.settings

import com.intellij.openapi.components.*
import icu.windea.pls.*

/**
 * PLS AI设置。可以在插件的对应设置页面中进行配置。
 */
@Service(Service.Level.APP)
@State(name = "PlsAiSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsAiSettings : SimplePersistentStateComponent<PlsAiSettingsState>(PlsAiSettingsState())

/**
 * @property enable 是否启用基于AI的各种功能。
 */
class PlsAiSettingsState : BaseState() {
    var enable by property(false)
}
