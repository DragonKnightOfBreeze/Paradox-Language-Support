package icu.windea.pls.integrations.settings

import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*

class PlsIntegrationsSettingsConfigurable : BoundConfigurable(PlsBundle.message("settings.integrations")), SearchableConfigurable {
    override fun getId() = "pls.integrations"

    override fun createPanel(): DialogPanel {
        val settings = PlsFacade.getIntegrationsSettings()
        return panel {
            //TODO 1.4.2
        }
    }
}
