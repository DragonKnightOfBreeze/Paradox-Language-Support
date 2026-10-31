package icu.windea.pls.integrations.lints

import icu.windea.pls.base.settings.ChronicleIntegrationSettings

object LintToolSettings {
    fun getInstance() = ChronicleIntegrationSettings.getInstance().state.lint
}
