package icu.windea.pls.ai.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.panel
import icu.windea.pls.PlsFacade
import icu.windea.pls.ai.PlsAiBundle

class PlsAiSettingsConfigurable : BoundConfigurable(PlsAiBundle.message("settings.ai")), SearchableConfigurable {
    override fun getId() = "pls.ai"

    override fun createPanel(): DialogPanel {
        val settings = PlsFacade.getAiSettings()
        return panel {
            TODO("Not yet implemented") //TODO dev
        }
    }
}
