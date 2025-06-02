package icu.windea.pls.integrations.settings

import com.intellij.openapi.components.*
import icu.windea.pls.PlsConstants

/**
 * PLS集成设置。
 *
 * 可以在插件的设置页面（`Settings > Languages & Frameworks > Paradox Language Support > Integrations`）中进行配置。
 */
@Service(Service.Level.APP)
@State(name = "PlsIntegrationsSettings", storages = [Storage(PlsConstants.pluginSettingsFileName)])
class PlsIntegrationsSettings : SimplePersistentStateComponent<PlsIntegrationsSettingsState>(PlsIntegrationsSettingsState())

class PlsIntegrationsSettingsState : BaseState() {
    //TODO 1.4.2
}
