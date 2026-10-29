package icu.windea.pls.integrations.images

import icu.windea.pls.base.settings.ChronicleIntegrationsSettings

object ImageToolSettings {
    fun getInstance() = ChronicleIntegrationsSettings.getInstance().state.image
}
