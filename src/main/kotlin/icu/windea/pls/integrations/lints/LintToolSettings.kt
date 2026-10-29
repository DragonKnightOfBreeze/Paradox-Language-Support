package icu.windea.pls.integrations.lints

import icu.windea.pls.base.settings.ChronicleIntegrationsSettings

object LintToolSettings {
    fun getInstance() = ChronicleIntegrationsSettings.getInstance().state.lint
}
