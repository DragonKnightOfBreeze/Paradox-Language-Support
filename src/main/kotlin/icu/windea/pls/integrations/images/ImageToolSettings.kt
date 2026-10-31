package icu.windea.pls.integrations.images

import icu.windea.pls.base.settings.ChronicleIntegrationSettings

object ImageToolSettings {
    fun getInstance() = ChronicleIntegrationSettings.getInstance().state.image
}
